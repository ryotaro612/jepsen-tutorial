(ns jepsen.etcd
  (:require [clojure.tools.logging :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [jepsen [cli :as cli]
             [control :as c]
             [db :as db]
             [tests :as tests]]
            [jepsen.control.util :as cu]
            [jepsen.os.debian :as debian]))

(defn db
  "Etcd DB for a particular version."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (info node "installing etcd" version))

    (teardown! [_ test node]
      (info node "tearing down etcd"))))

; setupで前のデータをリセットしたほうがいい
(defn etcd-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh,
  :concurrency, ...), constructs a test map."
  [opts]
  (println opts, "Hello, World!")
  (println (merge tests/noop-test
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
         {:concurrency 5, :leave-db-running? false, :logging-json? false, :ssh {:dummy? false, :username "root", :strict-host-key-checking false,:port 8022 :private-key-path (.getAbsolutePath (io/file ".." "id_rsa"))}
          :nodes ["127.0.0.1"], :test-count 1, :time-limit 60}

         ))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn etcd-test})
                   (cli/serve-cmd))
            args))

