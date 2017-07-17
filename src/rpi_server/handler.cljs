(ns rpi-server.handler
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [integrant.core :as ig]
            [cljs.nodejs :as nodejs]
            [clojure.walk :refer [keywordize-keys]]
            [cljs.core.async :refer [chan <! put!]]))

(defonce fs (js/require "fs"))

(def MESSAGE_ALLOC_SIZE 2)

(defonce process (atom nil))

(def WORK_DIR "./work/")

(defn- parse-message [buffer]
  (let [message-size (.readInt16BE buffer 0)
        message (.toString buffer "utf8" MESSAGE_ALLOC_SIZE
                           (+ MESSAGE_ALLOC_SIZE message-size))
        has-file? (> (.-length buffer)
                     (+ message-size MESSAGE_ALLOC_SIZE))]
    [(-> (.parse js/JSON message)
         js->clj
         keywordize-keys)
     (when has-file?
       (.slice buffer (+ message-size MESSAGE_ALLOC_SIZE)))]))

(defn- run-save [ch filename buffer]
  (.writeFile fs (str WORK_DIR filename) buffer (fn [err]
                                                  (if err
                                                    (put! ch {:err err})
                                                    (put! ch {:message "success"})))))

(defn- run-command [{:keys [cmd args]} file-buffer]
  (let [ch (chan)]
    (prn cmd args file-buffer)
    (condp = cmd
      "put"
      (run-save ch (:filename args) file-buffer)
      (throw (js/Error. "unknown command " cmd)))
    ch))

(defn make-handler [ws]
  (fn [message]
    (js/console.log "message received:" message)
    (go (let [[command file] (parse-message message)
              result (<! (run-command command file))]
          (.send ws (str  "result: " result))))))

(defmethod ig/init-key ::handler [_ _]
  make-handler)

(defn make-client-handler [ws]
  (fn [message]
    (js/console.log "[client] message received:" message)
    (.send ws message)))

(defmethod ig/init-key ::client-handler [_ _]
  make-client-handler)

(defn size-buffer [size]
  (let [size-buff (js/Buffer.allocUnsafe 2)]
    (.writeInt16BE size-buff size 0)
    size-buff))

(defn message-buffer [command & {:keys [args filename]}]
  (let [message (->  #js{:cmd command :args (clj->js args)}
                     js/JSON.stringify
                     js/Buffer.)]
    (if filename
      (js/Buffer.concat #js[(size-buffer (.-length message))
                            message
                            (fs.readFileSync filename)])
      (js/Buffer.concat #js[(size-buffer (.-length message)) message]))))

(def sample-with-file (message-buffer "hoge" :args {} :filename "./examples/sample.js"))
(def sample (message-buffer "hoge" :args {}))
