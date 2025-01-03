(ns jepsen.etcd
  (:require [clojure.tools.logging :as l]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [jepsen.control.docker :as docker]
            [jepsen [cli :as cli]
             [control :as c]
             [db :as db]
             [tests :as tests]]
            [jepsen.control.util :as cu]
            [jepsen.os.debian :as debian]))

(def node-map {"127.0.0.1:8379" {:client "http://172.20.0.2:2379"
                                 :peer "http://172.20.0.2:2380"
                                 :name "172.20.0.2"}
               "127.0.0.1:8380" {:client "http://172.20.0.3:2379"
                                 :peer "http://172.20.0.3:2380"
                                 :name "172.20.0.3"}
               "127.0.0.1:8381" {:client "http://172.20.0.4:2379"
                                 :peer "http://172.20.0.4:2380"
                                 :name "172.20.0.4"}})
; https://jepsen-io.github.io/jepsen/
(defn node-url
  "An HTTP url for connecting to a node on a particular port."
  [node port]
  (str "http://" node ":" port))

(defn peer-url
  "The HTTP url for other peers to talk to a node."
  [node]
  (node-url node 2380))

(defn client-url
  "The HTTP url clients use to talk to a node."
  [node]
  (node-url node 2379))

(defn initial-cluster
  "Constructs an initial cluster string for a test, like
  \"foo=foo:2380,bar=bar:2380,...\""
  [test]
  #_(l/info test)
  (->> (:nodes test)
       (map (fn [node]
              #_(str node "=" (peer-url node))
                (str (-> node node-map :name) "=" (-> node node-map :peer))))
       (str/join ",")))

(def etcd-dir     "/opt/etcd")
(def binary "etcd")
(def logfile (str etcd-dir "/etcd.log"))
(def pidfile (str etcd-dir "/etcd.pid"))

(defn db
  "Etcd DB for a particular version."
  [version]
  
  (reify db/DB
    (setup! [_ test node]
      (l/info node "installing etcd" version)
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
    (log-files [_ test node]
      [logfile])))

; setupで前のデータをリセットしたほうがいい
(defn etcd-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh,
  :concurrency, ...), constructs a test map."
  [opts]
  #_(println opts, "Hello, World!")
  #_(println (merge tests/noop-test
         {:pure-generators true}
         opts), "####")
  (db "")
  (merge tests/noop-test
         {
          :name "etcd"
          :os   debian/os
          :db   (db "v3.1.5")
          :pure-generators true}
         ; private-key-path must be an absolute path
         {:concurrency 5,
          :leave-db-running? false,
          :logging-json? false,
          ;; :ssh {:dummy? false,
          ;;       :username "root",
          ;;       :strict-host-key-checking false
          ;;       :port 8022
          ;;       :private-key-path (.getAbsolutePath (io/file ".." "id_rsa"))
          ;;       }
          :nodes ["127.0.0.1:8379", "127.0.0.1:8380", "127.0.0.1:8381"]
          :test-count 1
          :time-limit 60}
         {:remote docker/docker})
  )

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn etcd-test})
                   (cli/serve-cmd))
            args))

