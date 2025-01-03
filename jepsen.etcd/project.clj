(defproject jepsen.etcd "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 ;; https://mvnrepository.com/artifact/jepsen/jepsen
                 [jepsen/jepsen "0.3.7"]
                 [verschlimmbesserung "0.1.3"]
                 ]
  :main jepsen.etcd
  :repl-options {:init-ns jepsen.etcd})
