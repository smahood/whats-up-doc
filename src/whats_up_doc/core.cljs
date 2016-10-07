(ns whats-up-doc.core
  (:require [whats-up-doc.db]
            [whats-up-doc.events :as events]
            [whats-up-doc.subs :as subs]
            [whats-up-doc.views :as views]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :as re-frisk :refer-macros [export-debugger!]]))

(enable-console-print!)
(export-debugger!)


(defn mount-root []
  (reagent/render [views/main] (js/document.getElementById "app")))


(defn ^:export run
  [options]
  (let [clj-options (js->clj options :keywordize-keys true)]
    (re-frame/dispatch-sync [:initialize clj-options])
    (re-frisk/enable-re-frisk! {:x 300 :y 0})
    (mount-root)))

