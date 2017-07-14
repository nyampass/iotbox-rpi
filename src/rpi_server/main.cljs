(ns rpi-server.main
  (:require [integrant.core :as ig]))

(enable-console-print!)

(defmethod ig/init-key :app [_ _]
  (println "hi"))

(def configs
  {:app nil})

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
