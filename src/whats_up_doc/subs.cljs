(ns whats-up-doc.subs
  (:require [re-frame.core :as re-frame]
            [re-frisk.core :as re-frisk]))


(re-frame/reg-sub
  :font-size
  (fn [db _]
    (:font-size db)))


(re-frame/reg-sub
  :reading-panel
  (fn [db _]
    (:reading-panel db)))

(re-frame/reg-sub
  :toc-panel
  (fn [db _]
    (:toc-panel db)))

(re-frame/reg-sub
  :github-files
  (fn [db _]
    (:github-files db)))

(re-frame/reg-sub
  :initialized?
  (fn [db _]
    (:initialized? db)))


