(ns whats-up-doc.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer-macros [def-view]]
            [camel-snake-kebab.core :as kebab]
            [re-frisk.core :as re-frisk]
            [markdown.core :as markdown]
            [whats-up-doc.navigation-fx]))


;;;;;;;;;;;;;;
;; Top Menu ;;
;;;;;;;;;;;;;;


(defn top-menu []
  ;; TODO - Make logo customizable
  [:div.frow.justify-start.gutters.items-stretch.col-lg-11-12
   [:a {:href "http://re-frame.org"}
    [:img {:src   "img/re-frame_128w.png"
           :style {:margin "10px"}}]]

   [:div.frow.centered.top-menu
    [:a "Documentation"]]
   [:div.frow.centered.top-menu.hidden-xs
    [:a {:href "https://github.com/Day8/re-frame"}
     "View on GitHub"]]

   [:div.frow.centered.top-menu.hidden-xs
    [:a {:href "https://github.com/Day8/re-frame/issues"}
     "Issues"]]

   [:div.frow.centered.top-menu.hidden-sm.hidden-md.hidden-lg
    [:a {:href "#"}
     [:img {:src    "icons/ic_menu_black_24px.svg"
            :height "24px"}]]]])


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


(defn table-of-contents []
  (let [github-files (re-frame/subscribe [:github-files])
        toc-panel (re-frame/subscribe [:toc-panel])]
    [:ul
     [:li (str "Back button")]


     (doall
       (for [entry @toc-panel]
         ^{:key (str ":toc-panel/" (:index entry))}
         [render-toc-entry entry]))]))


;;;;;;;;;;;;;;;;;;;
;; Reading Frame ;;
;;;;;;;;;;;;;;;;;;;


(defn render-reading-panel
  "Render the reading frame"
  []
  (let [font-size (re-frame/subscribe [:font-size])
        reading-panel (re-frame/subscribe [:reading-panel])]
    [:div.reading-frame.col-xs-12-12.col-sm-9-12.col-md-9-12.col-lg-8-12
     [:div
      {:style {:padding-left "10px"
               :border       "solid 2px black"
               :font-size    (str @font-size "px")}}

      [:div.frow.justify-end.items-stretch
       [:img.clickable {:src      "icons/ic_zoom_out_black_24px.svg"
                        :height   "24px"
                        :on-click #(re-frame/dispatch [:decrease-font-size])}]
       [:img.clickable {:src      "icons/ic_zoom_in_black_24px.svg"
                        :height   "24px"
                        :on-click #(re-frame/dispatch [:increase-font-size])}]]
      [:div {:dangerouslySetInnerHTML {:__html (markdown/md->html @reading-panel)}}]]]))

;;;;;;;;;;;;;;;;;;;;
;; Page Rendering ;;
;;;;;;;;;;;;;;;;;;;;

(defn main [options]
  [:div
   [:div (str options)]
   [top-menu]
   [:div.frow.justify-start.items-stretch
    [:div.nav-sidebar.hidden-xs.col-sm-3-12.col-md-3-12.col-lg-3-12
     {:style {:border "solid 2px black"}}
     [table-of-contents]]
    [render-reading-panel]]])

