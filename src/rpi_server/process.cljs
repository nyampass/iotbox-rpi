(ns rpi-server.process
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async]))

(def spawn (.-spawn (nodejs/require "child_process")))

(defn start [cmd & {:keys [args callback dir]}]
  (prn cmd args callback dir)
  (try
    (let [process (spawn
                   cmd
                   (clj->js args)
                   #js{"cwd" dir})]
      (when callback
        (.stdout.on process "data"
                    #(callback :stdout (str %)))
        (.stderr.on process "data"
                    #(callback :error (str  %)))
        (.on process "exit"
             #(callback :message (str "Exit: " %)))
        (.on process "error"
             #(callback :error (str %))))
      process)
    (catch :default e
        (callback :err e)
        nil)))
