(defproject rpi-server "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.671"]
                 [org.clojure/core.async  "0.3.442"
                  :exclusions [org.clojure/tools.reader]]
                 [integrant "0.4.0"]]

  :plugins [[lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "rpi-server.main/reload"}
                :compiler {:main rpi-server.main
                           :optimizations :none
                           :target :nodejs
                           :output-to "app.js"
                           :output-dir "target/dev/out"
                           :source-map true
                           :source-map-timestamp true}}
               {:id "app"
                :source-paths ["src"]
                :compiler {:main rpi-server.main
                           :optimizations :none
                           :target :nodejs
                           :output-to "app.js"
                           :output-dir "target/app/out"}}]}

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["app.js" "dev.js" :target-path]}})
