(ns whats-up-doc.navigation-fx
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]
            [camel-snake-kebab.core :as kebab]
            ))

;(re-frame/reg-fx
;  :toc/navigate
;  (fn [toc-entry]
;    (re-frisk/add-in-data [:debug :toc :toc/navigate] {:toc-entry toc-entry})
;    {}
;    ))

(re-frame/reg-event-fx
  :toc/navigate-fx
  (fn [{:keys [db]} [_ toc-entry]]
    (re-frisk/add-in-data [:debug :toc :toc/navigate-fx] {:db        db
                                                          :toc-entry toc-entry})
    (if-let [file (get-in db [:github-files (:path toc-entry)])]
      {:db (assoc db :reading-panel (:markdown file))}
      {})))








(re-frame/reg-event-fx
  :reading/navigate-fx
  (fn [cofx]
    {}))