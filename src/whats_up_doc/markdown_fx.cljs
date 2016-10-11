(ns whats-up-doc.markdown-fx
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]
            [camel-snake-kebab.core :as kebab]
            [markdown.core :as markdown-clj]))


(re-frame/reg-event-fx
  :markdown->toc-panel
  ;; TODO - pass in markdown content, parse it into toc data formmat and update :toc-panel with it
  (fn [cofx]
    {}
    ))

(re-frame/reg-event-fx
  :markdown->reading-panel
  ;; TODO - pass in markdown content, parse it into something renderable, and update :reading-panel with it
  (fn [cofx]
    {}
    ))