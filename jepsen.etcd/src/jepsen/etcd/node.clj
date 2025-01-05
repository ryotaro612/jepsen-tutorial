(ns jepsen.etcd.node)

(defn- make-container-address
  [ipv4]
  {:client (str "http://" ipv4 ":2379")
   :peer (str "http://" ipv4 ":2380")
   :name ipv4})

(def node-map
  {"127.0.0.1:8379" (make-container-address "172.20.0.2")
   "127.0.0.1:8380" (make-container-address "172.20.0.3")
   "127.0.0.1:8381" (make-container-address "172.20.0.4")
   "127.0.0.1:8382" (make-container-address "172.20.0.5")
   "127.0.0.1:8383" (make-container-address "172.20.0.6")
   })
