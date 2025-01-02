(ns jepsen.etcd
  (:require [jepsen.cli :as cli]
            [jepsen.db :as db]
            [jepsen.tests :as tests]))

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
         {:pure-generators true}
         ; private-key-path must be an absolute path
         {:concurrency 5, :leave-db-running? false, :logging-json? false, :ssh {:dummy? false, :username "root", :strict-host-key-checking false, :port 8022 :private-key-path "~/.ssh/id_rsa"},  :nodes ["127.0.0.1"], :test-count 1, :time-limit 60}
         ))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn etcd-test})
                   (cli/serve-cmd))
            args))

