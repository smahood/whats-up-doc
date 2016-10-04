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
  (fn [cofx]
    (re-frisk/add-in-data [:debug :toc :toc/navigate-fx] {:cofx cofx})
    {}))