(ns jepsen.etcd.db
  (:require [clojure.tools.logging :as l]
            [jepsen.control.util :as cu]            
            [jepsen
             [control :as c]             
             [db :as db]]))


(def etcd-dir     "/opt/etcd")
(def binary "etcd")
(def logfile (str etcd-dir "/etcd.log"))
(def pidfile (str etcd-dir "/etcd.pid"))

(defrecord Etcd
    []
  ;An instance that starts and stops an etcd server.
  db/DB
  (setup! [_ test node]
      (l/info node "installing etcd")
      (c/su
       (cu/start-daemon!
        {:logfile logfile
         :pidfile pidfile
         :chdir   etcd-dir}
        binary
        :--log-outputs                  :stderr
        :--name                         (-> node node-map :name) ;(node-map node)
        :--listen-peer-urls             (-> node node-map :peer) ;(peer-url node)
        :--listen-client-urls           (-> node node-map :client) ;(client-url node)
        :--advertise-client-urls        (-> node node-map :client) ;(client-url node)
        :--initial-cluster-state        :new
        :--initial-advertise-peer-urls  (-> node node-map :peer) ;(peer-url node)
        :--initial-cluster              (initial-cluster test))
       (Thread/sleep 10000)))
  (teardown! [_ test node]
      (l/info node "tearing down etcd")
      (cu/stop-daemon! binary pidfile))
  db/LogFiles
  (log-files [_ test node-]
    [logfile]))

