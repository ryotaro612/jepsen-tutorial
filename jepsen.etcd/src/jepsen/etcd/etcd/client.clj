(ns jepsen.etcd.etcd.client
  (:require [clojure.tools.logging :as l])
  (:import [io.etcd.jetcd ClientBuilder Client KeyValue ByteSequence]
           [java.net URI]
           [io.etcd.jetcd.options PutOption]
           [io.etcd.jetcd.op Cmp CmpTarget Op Cmp$Op CmpTarget$ValueCmpTarget]
           [java.util.concurrent TimeUnit]
           [java.nio.charset StandardCharsets]))

(defn put!
  "The type of k and v is byte[]."
  [client timeout-mills k v]
  (let [key-bytes (ByteSequence/from k)
        value-bytes (ByteSequence/from v)
        response (.put client key-bytes value-bytes)]
    (.get response timeout-mills TimeUnit/MILLISECONDS)))

(defn get-etcd
  "k is key, client is kv-client, timeout-millis is timeout in milliseconds."
  [k client timeout-millis]
  (let [key-bytes (ByteSequence/from k)
        completable-future (.get client key-bytes)
        get-response (.get completable-future timeout-millis TimeUnit/MILLISECONDS)]
    (if (< 0 (.getCount get-response))
      (map #(-> % .getValue .getBytes) (.getKvs get-response)))))

(defn cas!
  ""
  [client timeout-mills k prev v]
  (let [txn (.txn client)
        key-bytes (ByteSequence/from k)
        prev-bytes (ByteSequence/from prev)
        value-bytes (ByteSequence/from v)
        cmp (new Cmp key-bytes Cmp$Op/EQUAL (CmpTarget$ValueCmpTarget/value prev-bytes))
        if-txn (. txn If (into-array [cmp]))
        then (. if-txn Then (into-array [(Op/put key-bytes value-bytes  PutOption/DEFAULT)]))
        result (.get (.commit then) timeout-mills TimeUnit/MILLISECONDS)]
    (.isSucceeded result)))

(defn- make-etcd-cluster-client
  [endpoints]
  (let [builder (Client/builder)
        uris (map #(new URI %) endpoints)
        client (.build (.endpoints builder uris))]
    client))

(defn- etcd-client->kv-client
  [client]
  (.getKVClient client))

(defn endpoints->kv-client
  ""
  [endpoints]
  (-> endpoints
      make-etcd-cluster-client
      etcd-client->kv-client))
