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


;; Really, I want to be able to pass in the markdown and parse it into hiccup (for a nice view) - seems like overkill

;; What does this all have to handle?
;; - Eager loading folders and documents
;; - Lazy loading documents
;; - Option for expandable links in ToC
;; - Option for links in ToC to change reading panel only


;; Experiment 1
;; - When clicking on ToC link, change reading panel
;; - When clicking on reading panel link, change ToC to match parent of clicked link and reading-panel to match clicked link

;; Experiment 2
;; - Click on link in ToC, expand and load data in ToC, load data to reading-panel and move browser to it

;; Experiment 3
;; - ToC for root doc on left, ToC for child on 3rd panel on right

;; Experiment 4
;; - Load one doc at a time based on how close to end (or clicking on link on Left)

