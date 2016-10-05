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
    (let [new-toc-entry
          (if (:expanded toc-entry) (assoc toc-entry :expanded false)
          (assoc toc-entry :expanded true))]
      {:github/file (:url toc-entry)
       :db          (assoc db
                      :toc-panel
                      (assoc
                        (:toc-panel db)
                        (:index toc-entry)
                        new-toc-entry))})))



(re-frame/reg-event-fx
  :reading/navigate-fx
  (fn [cofx]
    {}))