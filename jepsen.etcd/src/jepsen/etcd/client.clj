(ns jepsen.etcd.client
  (:require [clojure.tools.logging :as l]            
            [clojure.string :as str]
            [jepsen [client :as client]]
            [verschlimmbesserung.core :as v]))

(defn r   [_ _] {:type :invoke, :f :read, :value nil})
(defn w   [_ _] {:type :invoke, :f :write, :value (rand-int 5)})
(defn cas [_ _] {:type :invoke, :f :cas, :value [(rand-int 5) (rand-int 5)]})


(defrecord Client [node-map conn]
  client/Client
  (open! [this test node]
    (assoc this :conn (v/connect (-> node node-map :client)
                                 {:timeout 5000})))

  (setup! [this test])

  (invoke! [this test op]
    ;; (l/info "invoke!" this)
    ;; (l/info "invoke2!" node-map conn)    
    (case (:f op)
      :read (assoc op :type :ok, :value (v/get conn "foo"))))

  (teardown! [this test])

  (close! [_ test]))


