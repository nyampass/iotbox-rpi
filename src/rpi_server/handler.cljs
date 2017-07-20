(ns rpi-server.handler
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [integrant.core :as ig]
            [cljs.nodejs :as nodejs]
            [clojure.walk :refer [keywordize-keys]]
            [cljs.core.async :refer [chan <! put!]]
            [rpi-server.process :as process]))

(defonce fs (js/require "fs"))

(def MESSAGE_ALLOC_SIZE 2)

(defonce latest-process (atom nil))

(defn start-process [callback]
  (prn :start-process latest-process)
  (if-let [process @latest-process]
    (do
      (prn process :kill)
      (.kill process))
    )
  (let [process (process/start "node" :args ["work/index.js"]
                               :callback callback)]
    (prn :process process)
    (reset! latest-process process)))

(def WORK_DIR "./work/")

(defn- parse-message [buffer]
  (let [message-size (.readUInt16BE buffer 0)
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

(defn- run-command [ws {:keys [cmd args]} file-buffer]
  (let [ch (chan)]
    (prn cmd args file-buffer)
    (condp = (keyword cmd)
      :put
      (run-save ch (:filename args) file-buffer)
      :run
      (do
        (start-process (fn [type message]
                                   (if (= (.-readyState ws)
                                          (.-OPEN ws))
                                     (.send ws
                                            (str :log type message)))))
        (put! ch {:message "success"}))
      (throw (js/Error. "unknown command " cmd)))
    ch))

(defn make-handler [ws]
  (fn [message]
    (js/console.log "message received:" message)
    (go (let [[command file] (parse-message message)
              result (<! (run-command ws command file))]
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
    (.writeUInt16BE size-buff size 0)
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
