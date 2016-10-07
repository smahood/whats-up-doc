(ns whats-up-doc.navigation-fx
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]
            [camel-snake-kebab.core :as kebab]
            [whats-up-doc.localstorage :as localstorage]
            ))

;(re-frame/reg-fx
;  :toc/navigate
;  (fn [toc-entry]
;    (re-frisk/add-in-data [:debug :toc :toc/navigate] {:toc-entry toc-entry})
;    {}
;    ))


(re-frame/reg-event-fx
  :toc/navigate-fx
  [localstorage/intercept-key localstorage/int2]
  (fn [{:keys [db localstorage]} [_ toc-entry]]
    (re-frisk/add-in-data [:debug :toc :toc/navigate-fx] {:db        db
                                                          :localstorage localstorage
                                                          :toc-entry toc-entry})
    (if-let [file (get-in db [:github-files (:path toc-entry)])]
      {:db (assoc-in db [:reading-panel :markdown] (:markdown file))}
      {})))


(re-frame/reg-event-fx
  :reading/navigate-fx
  [re-frame/debug localstorage/intercept-key localstorage/int2]
  (fn [cofx]
    (println ":navigate-fx int1 int2")
    (re-frisk/add-in-data [:debug :toc :reading/navigate-fx] {:cofx cofx})
    {}))
