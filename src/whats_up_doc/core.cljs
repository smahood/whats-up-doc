(ns whats-up-doc.core
  (:require [whats-up-doc.db]
            [whats-up-doc.events :as events]
            [whats-up-doc.subs :as subs]
            [whats-up-doc.views :as views]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :as re-frisk :refer-macros [export-debugger!]]
            [goog.History :as h]
            [goog.events :as e])
  (:import [goog History]))

(enable-console-print!)
(export-debugger!)


(defn mount-root []
  (reagent/render [views/main] (js/document.getElementById "app")))

(defn init-history! []
  (let [history (History.)]

    ;(println .getToken history)

    (e/listen history h/EventType.NAVIGATE
              (fn [e]
                (re-frame/dispatch [:link/navigate-fx (.-token e)])
                ;           (println (js-keys e))
                ;(println (.-type e))
                ;(println (js-keys (.-target e)))
                ;(println (.-currentTarget e))
                ;(println (.-propagationStopped_ e))
                ;(println (.-defaultPrevented e))
                ;(println (.-returnValue_ e))
                ;          (println (.-token e))
                ;(println (.-isNavigation e))
                ;(println (.-constructor e))
                ;(println (.-stopPropagation e))
                ;(println (.-preventDefault e))
                ))
    (.setEnabled history true)))



(defn ^:export run
  [options]
  (let [clj-options (js->clj options :keywordize-keys true)]
    (init-history!)
    (re-frame/dispatch-sync [:initialize clj-options])
    (re-frisk/enable-re-frisk! {:x 300 :y 0})
    (mount-root)))
