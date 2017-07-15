(ns rpi-server.main
  (:require [integrant.core :as ig]
            [rpi-server.websocket :as websocket]
            [rpi-server.handler :as handler]))

(enable-console-print!)

(def configs
  {::handler/handler {}
   ::websocket/server {:port 3000 :handler (ig/ref ::handler/handler)}})

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
