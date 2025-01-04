(ns jepsen.etcd.etcd.client
  (:require [clojure.tools.logging :as l])
  (:import [io.etcd.jetcd ClientBuilder Client KeyValue ByteSequence]
           [java.net URI]
           [java.util.concurrent TimeUnit]
           [java.nio.charset StandardCharsets]))

(defn put
  ""
  [client timeout-mills k v]
  (let [key-bytes (ByteSequence/from k)
        value-bytes (ByteSequence/from v)
        response (.put client key-bytes value-bytes)]
    (.get response timeout-mills TimeUnit/MILLISECONDS)))

(defn get
  "k is key, client is kv-client, timeout-millis is timeout in milliseconds."
  [k client timeout-millis]
  (let [key-bytes (ByteSequence/from k)
        completable-future (.get client key-bytes)
        get-response (.get completable-future timeout-millis TimeUnit/MILLISECONDS)
        ]
    (if (.getCount get-response)
      (map #(-> % .getValue .getBytes) (.getKvs get-response)))))

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
