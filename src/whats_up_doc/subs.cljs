(ns whats-up-doc.subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
  :font-size
  (fn [db _]
    (:font-size db)))


(re-frame/reg-sub
  :reading-frame-content
  (fn [db _]
    (:reading-frame-content db)))


(re-frame/reg-sub
  :github-files
  (fn [db _]
    (:github-files db)))