(ns jepsen.etcd.client
  (:require [clojure.tools.logging :as l]            
            [clojure.string :as str]
            [jepsen [client :as client]]
            [jepsen.etcd.etcd.client :as ec]))

(defn r   [_ _] {:type :invoke, :f :read, :value nil})
(defn w   [_ _] {:type :invoke, :f :write, :value (rand-int 5)})
(defn cas [_ _] {:type :invoke, :f :cas, :value [(rand-int 5) (rand-int 5)]})

(defrecord Client [node-map client]
  client/Client
  (open! [this test node]
    (assoc this :client (ec/endpoints->kv-client (map #(str "http://" %) (keys node-map)))))
  
  (setup! [this test])
  
  (invoke! [_ test op]
    (case (:f op)
      :read (let [res (ec/get (.getBytes "foo") client 3000)
                  value (if res (Integer/parseInt (new String (first res))))]
              (l/info {:invoke-read value})
              (assoc op :type :ok, :value value))
      :write (do
               (ec/put client 3000 (.getBytes "foo") (.getBytes (str (:value op))))
               (assoc op :type :ok))
      ))

  (teardown! [this test])

  (close! [_ test]))
