(ns rpi-server.handler
  (:require [integrant.core :as ig]))

(defn make-handler [ws]
  (fn [message]
    (js/console.log "message received:" message)
    (.send ws message)))

(defmethod ig/init-key ::handler [_ _]
  make-handler)
