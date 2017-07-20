(ns rpi-server.process
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async]))

(def spawn (.-spawn (nodejs/require "child_process")))

(defn start [cmd & {:keys [args callback]}]
  (try
    (let [process (spawn cmd (clj->js args))]
      (when callback
        (.stdout.on process "data"
                    #(callback :stdout %))
        (.stderr.on process "data"
                    #(callback :stderr %))
        (.on process "exit"
             #(callback :exit %))
        (.on process "error"
             #(callback :err  %)))
      process)
    (catch :default e
        (callback :err e)
        nil)))
