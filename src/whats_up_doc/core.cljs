(ns whats-up-doc.core
  (:require [whats-up-doc.db]
            [whats-up-doc.events :as events]
            [whats-up-doc.subs :as subs]
            [whats-up-doc.views :as views]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer [enable-re-frisk!]]))


(enable-re-frisk!  {:x 10 :y 300})


(enable-console-print!)


(defn mount-root []
   (reagent/render [views/main]
                  (js/document.getElementById "app")))


;; TODO - specify map of options that can be called from JS
;; Loading - Lazy or Eager
;; Root document - pass root doc in here, mandatory field
;; Option to display all docs as single stream or separate files
;; Would also be nice to have some graphical options - starting font size,
;; CSS classes, etc - or ways to override CSS classes so that instead of
;; using the default class you can provide your own classes or something
;; It would be very cool to be able to insert interceptors or middleware
;; that can be run when certain things are done (parsing or rendering the
;; TOC or markdown, etc.) - it would be interesting to think about whether
;; a plugin style architecture is at all reasonable, where all the rendering
;; or parsing is a specific plugin, or can plug in different fetching and
;; file parsing rules or something like that.
;; Debug or pre-deployment mode - check what kind of download sizes there
;; are, run against specs, etc. - maybe hook into frisk or similar
;; Setup nice error messages for malformed specs like figwheel has
;; for the config options
;; What kind of error messages are reasonable for end users to see?


(defn ^:export run
  [options]
  (mount-root)
  (re-frame/dispatch-sync [:initialize options])
 )
