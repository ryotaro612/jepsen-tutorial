(ns jepsen.etcd
  (:require [clojure.tools.logging :as l]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [jepsen.control.docker :as docker]
            [jepsen.checker.timeline :as timeline]
            [jepsen
             [checker :as checker]
             [cli :as cli]
             [generator :as gen]
             [control :as c]
             [db :as db]
             [tests :as tests]
             [client :as client]]
            [jepsen.control.util :as cu]
            [jepsen.os.debian :as debian]
            [jepsen.etcd.client :as ec]
            [jepsen.etcd.db :as edb]
            [knossos.model :as model]
            [jepsen.etcd.node :as n]))

                                        ; setupで前のデータをリセットしたほうがいい
(defn etcd-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh,
  :concurrency, ...), constructs a test map."
  [opts]
  (merge tests/noop-test
         {:name "etcd"
          :os   debian/os
          :db   (edb/map->Etcd {:node-map n/node-map})
          :pure-generators true
          :client (ec/map->Client {:node-map n/node-map})
          :checker (checker/compose
                    {:perf   (checker/perf)
                     :linear (checker/linearizable {:model     (model/cas-register)
                                                    :algorithm :linear})
                     :timeline (timeline/html)})
          :generator       (->> (gen/mix [ec/r ec/w ec/cas])
                                #_(gen/stagger 1)
                                (gen/stagger 1/50)                                
                                (gen/nemesis
                                 (cycle [(gen/sleep 5)
                                         {:type :info, :f :start}
                                         (gen/sleep 5)
                                         {:type :info, :f :stop}]))
                                #_(gen/nemesis nil)
                                (gen/time-limit 30))}
         
         {:concurrency 5
          :leave-db-running? false
          :logging-json? false
          :nodes ["127.0.0.1:8379"
                  "127.0.0.1:8380"
                  "127.0.0.1:8381"
                  "127.0.0.1:8382"
                  "127.0.0.1:8383"]
          :test-count 1
          :time-limit 15}
         {:remote docker/docker}))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn etcd-test})
                   ; launch a web server that serves the results of the tests
                   (cli/serve-cmd))
            args))

