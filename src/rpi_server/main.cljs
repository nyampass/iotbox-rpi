(ns rpi-server.main
  (:require [integrant.core :as ig]
            [rpi-server.websocket :as websocket]
            [rpi-server.handler :as handler]))

(enable-console-print!)

(def configs
  {::handler/handler {}
   ::websocket/server {:port 3030 :handler (ig/ref ::handler/handler)}
   ::handler/client-handler {}
   ::websocket/client {:url "ws://localhost:3000/ws"
                       :handler (ig/ref ::handler/client-handler)}})

(defonce system (atom nil))

(defn start []
  (when-not @system
    (reset! system (ig/init configs))))

(defn stop []
  (when @system
    (ig/halt! @system)
    (reset! system nil)))

(defn main []
  (start))

(defn reload []
  (stop)
  (start))

(set! *main-cli-fn* main)
