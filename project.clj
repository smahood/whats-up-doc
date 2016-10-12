(defproject whats-up-doc "0.1.1-SNAPSHOT"
  :description "A reader for GitHub documentation built in ClojureScript with re-frame"

  :url "http://whats-up-doc.org"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [re-frame "0.8.0"]
                 [day8.re-frame/http-fx "0.0.4"]
                 [re-frisk "0.2.2"]
                 [cljs-ajax "0.5.8"]
                 [camel-snake-kebab "0.4.0"]
                 [markdown-clj "0.9.89"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [org.clojure/core.async "0.2.391"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-figwheel "0.5.8"]
            [lein-ancient "0.6.10"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src"]
                :figwheel     {:on-jsload "whats-up-doc.core/mount-root"
                               :open-urls ["http://localhost:3449/index.html"]}
                :compiler     {:main                 whats-up-doc.core
                               :asset-path           "js/compiled/out"
                               :output-to            "resources/public/js/compiled/whats_up_doc.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :source-map-timestamp true
                               :external-config      {:re-frisk {:enabled     true
                                                                 :script-path "js/compiled/whats_up_doc.js"}}
                               :preloads             [devtools.preload]}}
               {:id           "min"
                :source-paths ["src"]
                :compiler     {:output-to     "resources/public/js/compiled/whats_up_doc.js"
                               :main          whats-up-doc.core
                               :optimizations :advanced
                               :pretty-print  false}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:init             (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})