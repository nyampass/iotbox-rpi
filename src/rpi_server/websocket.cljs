(ns rpi-server.websocket
  (:require [cljs.nodejs :as nodejs]
            [integrant.core :as ig]))

(defonce WebSocket (nodejs/require "ws"))

(defn make-server [{:keys [handler port]}]
  (doto (WebSocket.Server. #js{:port port})
    (.on "connection"
         (fn [ws]
           (.on ws "message" (handler ws))))))

(defn make-client [{:keys [handler url]}]
  (doto (WebSocket. url nil)
    (.on "connection"
         (fn [ws]
           (.on ws "data" (handler ws))))))

(defmethod ig/init-key ::server [_ opts]
  (println "starting websocket server ....")
  (make-server opts))

(defmethod ig/halt-key! ::server [_ server]
  (println "stopping websocket server ...")
  (.close server))

(defmethod ig/init-key ::client [_ opts]
  (println "starting websocket client ....")
  (make-client opts))

(defmethod ig/halt-key! ::client [_ client]
  (println "stopping websocket client ...")
  (.close client))

