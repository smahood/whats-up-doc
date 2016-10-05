(ns whats-up-doc.views
  (:require [reagent.core]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer-macros [def-view]]
            [re-frisk.core :as re-frisk]
            [markdown.core :as markdown]
            [whats-up-doc.navigation-fx]))


;;;;;;;;;;;;;;;;;;;;;;;
;; Table of Contents ;;
;;;;;;;;;;;;;;;;;;;;;;;


(declare render-toc-entry)


(defn render-toc-heading
  "Renders markdown headings"
  [entry]
  (re-frisk/add-in-data [:debug :toc :render-toc-heading] entry)
  [:li.toc-entry.heading [:span {:style {:font-weight "bold"}} (:display entry)]])


(defn render-toc-link
  "Renders markdown links"
  [entry]
  (re-frisk/add-in-data [:debug :toc :render-toc-link] entry)
  [:ul.nested
   [:li
    [:a
     {:href     "#"
      :on-click #(re-frame/dispatch [:toc/navigate-fx entry])}
     (:display entry)]]])


(defn render-toc-entry [entry]
  (cond
    (= (:type entry) "heading") [render-toc-heading entry]
    (= (:type entry) "link") [render-toc-link entry]
    (= [:type entry] "") [:li "No Type" (str entry)]
    :else [:li "Other Type - " (:type entry) (str entry)]))


(defn toc-panel []
  (let [toc-panel (re-frame/subscribe [:toc-panel])
        font-size (re-frame/subscribe [:font-size])]
    [:div.toc-panel.hidden-xs.col-sm-3-12.col-md-3-12.col-lg-3-12
     {:style {:border    "solid 2px black"
              :max-width "40ch"
              :font-size (str @font-size "px")}}
     [:br]
     [:br]
     [:br]
     [:ul
      (doall
        (for [entry @toc-panel]
          ^{:key (str ":toc-panel/" (:index entry))}
          [render-toc-entry entry]))]]))


;;;;;;;;;;;;;;;;;;;
;; Reading Frame ;;
;;;;;;;;;;;;;;;;;;;


(defn reading-panel
  "Render the reading frame"
  []
  (let [reading-panel (re-frame/subscribe [:reading-panel])
        font-size (re-frame/subscribe [:font-size])]
    [:div.reading-panel.col-xs-12-12.col-sm-9-12.col-md-9-12.col-lg-8-12
     [:div
      {:style {:padding-left "10px"
               :border       "solid 2px black"
               :max-width    "80ch"
               :border-left  "0px"
               :font-size    (str @font-size "px")
               :margin-right "10px"
               :padding-right "10px"}}

      [:div.frow.justify-end.items-stretch
       [:img.clickable {:src      "icons/ic_zoom_out_black_24px.svg"
                        :height   "24px"
                        :on-click #(re-frame/dispatch [:decrease-font-size])}]
       [:img.clickable {:src      "icons/ic_zoom_in_black_24px.svg"
                        :height   "24px"
                        :on-click #(re-frame/dispatch [:increase-font-size])}]]
      [:div {:dangerouslySetInnerHTML {:__html (markdown/md->html @reading-panel
                                                                  :heading-anchors true
                                                                  :reference-links? true)}}]
      [:br]]]))

;;;;;;;;;;;;;;;;;;;;
;; Page Rendering ;;
;;;;;;;;;;;;;;;;;;;;

(defn main []
  (let [initialized? (re-frame/subscribe [:initialized?])]
    (if @initialized?
      [:div.main
       [:div.frow.justify-start.items-stretch
        [toc-panel]
        [reading-panel]]]
      )))