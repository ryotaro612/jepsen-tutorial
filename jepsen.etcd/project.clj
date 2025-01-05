(defproject jepsen.etcd "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 ;; https://mvnrepository.com/artifact/jepsen/jepsen
                 [jepsen/jepsen "0.3.7"]
                 ;; https://mvnrepository.com/artifact/io.etcd/jetcd-core
                 [io.etcd/jetcd-core "0.8.4"]
                 ;; https://mvnrepository.com/artifact/slingshot/slingshot
                 [slingshot/slingshot "0.12.2"]]
  :plugins [[dev.weavejester/lein-cljfmt "0.13.0"]]
  :main jepsen.etcd
  :repl-options {:init-ns jepsen.etcd})
