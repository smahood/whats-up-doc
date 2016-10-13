(ns whats-up-doc.navigation-fx
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]
            [camel-snake-kebab.core :as kebab]
            ))

(re-frame/reg-event-fx
  :toc/navigate-fx
  (fn [{:keys [db]} [_ toc-entry]]
    (re-frisk/add-in-data [:debug :toc :toc/navigate-fx] {:db        db
                                                          :toc-entry toc-entry})
    {:github/file [(:url toc-entry) (:path toc-entry)]}))


(re-frame/reg-event-fx
  :reading/navigate-fx
  [re-frame/debug]
  (fn [cofx]
    (re-frisk/add-in-data [:debug :reading :reading/navigate-fx] {:cofx cofx})
    {}))