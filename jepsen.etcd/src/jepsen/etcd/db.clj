(ns jepsen.etcd.db
  (:require [clojure.tools.logging :as l]
            [jepsen.control.util :as cu]
            [clojure.string :as str]
            [jepsen.etcd.node :as n]
            [jepsen
             [control :as c]
             [db :as db]]))

(def ^:private etcd-dir  "/opt/etcd")
(def ^:private binary "etcd")
(def ^:private logfile (str etcd-dir "/etcd.log"))
(def ^:private pidfile (str etcd-dir "/etcd.pid"))

(defn- initial-cluster
  "Constructs an initial cluster string for a test, like
  \"foo=foo:2380,bar=bar:2380,...\""
  [node-map test]
  (->> (:nodes test)
       (map (fn [node]
              (str (-> node node-map :name) "=" (-> node node-map :peer))))
       (str/join ",")))

(defrecord Etcd
           [node-map]
  ;An instance that starts and stops an etcd server.
  db/DB
  (setup! [_ test node]
    (l/info node "installing etcd")
    (c/su
      (c/exec :rm :-rf etcd-dir)
      (cu/install-archive! "file:///usr/src/etcd.tar.gz" etcd-dir)
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
      :--initial-cluster              (initial-cluster n/node-map test))
     (Thread/sleep 10000)))
  (teardown! [_ test node]
    (l/info node "tearing down etcd")
    (cu/stop-daemon! binary pidfile))
  db/LogFiles
  (log-files [_ test node-]
    [logfile]))
