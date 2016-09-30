(ns whats-up-doc.core
  (:require [whats-up-doc.db]
            [whats-up-doc.events :as events]
            [whats-up-doc.subs :as subs]
            [whats-up-doc.views :as views]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer [enable-re-frisk!]]
            ))


(enable-re-frisk!)

;
;(def test-content "### Introduction\n- [re-frame Introduction](../README.md)\n\n\n### Understanding Event Handlers\n- [-db Event Handlers] TODO \n- [Effectful Handlers](EffectfulHandlers.md) \n- [Interceptors](Interceptors.md) \n- [Effects](Effects.md) \n- [Coeffects](Coeffects.md) \n\n\n### Structuring Your Application\n\n- [Basic App Structure](Basic-App-Structure.md)\n- [Navigation](Navigation.md)\n- [Namespaced Keywords](Namespaced-Keywords.md)\n\n\n### Populating Your Application Data\n\n- [Loading Initial Data](Loading-Initial-Data.md)\n- [Talking To Servers](Talking-To-Servers.md)\n- [Subscribing to External Data](Subscribing-To-External-Data.md)\n\n\n### Debugging And Testing \n\n- [Debugging-Event-Handlers](Debugging-Event-Handlers.md)\n- [Debugging](Debugging.md)\n\n\n### Miscellaneous\n- [FAQs](FAQs/README.md)\n- [External Resources](External-Resources.md)\n- [Eek! Performance Problems](Performance-Problems.md)\n- [Solve the CPU hog problem](Solve-the-CPU-hog-problem.md)\n- [Using Stateful JS Components](Using-Stateful-JS-Components.md)\n- [The re-frame Logo](The-re-frame-logo.md)\n")
;
;
;(def string2 "[test]")
;
;
;(re-seq #"\[.*\]\(.*\)" test-content)
;



;(re-frame/reg-event-db
;  :get-markdown
;  (fn [db [_ uri]]
;    (
;      ;; use the github API to retrieve the markdown content
;      ;; and populate the :markdown-content key in app-db
;
;
;      )))



;; Initialization steps
;; Initialize app-db defaults using dispatch-sync
;; Load starting view data as quick as possible - want first render complete and quick
;; Start recursive loading of docs - get contents from folder, then load readme, then rest of files
;; When data comes in, read markdown to be able to generate new ToC and possibly transform to HTML
;; At some point in process, render the HTML to the screen






;
;
;(defn greeting [message]
;  [:h1 message])
;
;
;(defn clock
;  []
;  (let [time-color (re-frame/subscribe [:time-color])
;        timer (re-frame/subscribe [:timer])]
;    (fn clock-render
;      []
;      (let [time-str (-> @timer
;                         .toTimeString
;                         (clojure.string/split " ")
;                         first)
;            style {:style {:color @time-color}}]
;        [:div.example-clock style time-str]))))
;
;
;(defn color-input
;  []
;  (let [time-color (re-frame/subscribe [:time-color])]
;    (fn color-input-render
;      []
;      [:div.color-input
;       "Time color: "
;       [:input {:type      "text"
;                :value     @time-color
;                :on-change #(re-frame/dispatch
;                             [:time-color (-> % .-target .-value)])}]])))
;
;




(enable-console-print!)
;
;(defn on-js-reload []
;  ;; optionally touch your app-state to force rerendering depending on
;  ;; your application
;  ;; (swap! app-state update-in [:__figwheel_counter] inc)
;  )

(defn mount-root []
   (reagent/render [views/main]
                  (js/document.getElementById "app")))



(defn ^:export run
  []
  (mount-root)
  (re-frame/dispatch-sync [:initialize])
 )
