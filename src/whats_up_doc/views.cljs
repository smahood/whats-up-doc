(ns whats-up-doc.views
  (:require [reagent.core]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer-macros [def-view]]
            [re-frisk.core :as re-frisk]
            [markdown.core :as markdown]
            [markdown.transformers :refer [transformer-vector]]
            [whats-up-doc.navigation-fx]))


;;;;;;;;;;;;;;;;;;;;;;;
;; Table of Contents ;;
;;;;;;;;;;;;;;;;;;;;;;;


(declare render-toc-entry)


(defn render-toc-heading
  "Renders markdown headings"
  [entry]
  (re-frisk/add-in-data [:debug :toc :render-toc-heading] entry)
  [:li.toc-entry.heading
   [:span
    {:style    {:font-weight "bold"}
     :on-click #(re-frame/dispatch [:reading/navigate-fx entry])}
    (:display entry)]])


(defn render-toc-link
  "Renders markdown links"
  [entry]
  (re-frisk/add-in-data [:debug :toc :toc/render-toc-link] entry)
  [:ul.nested
   [:li
    [:a
     {:href     (str "#" (:link entry))
      :on-click #(re-frame/dispatch [:toc/navigate-fx entry])}
     (:display entry)]]])


(defn render-toc-entry [entry]
  (cond
    (= (:type entry) "heading") [render-toc-heading entry]
    (= (:type entry) "link") [render-toc-link entry]
    (= [:type entry] "") [:li "No Type" (str entry)]
    :else [:li "Other Type - " (:type entry) (str entry)]))

(defn render-toc-title [entry]
  (re-frisk/add-in-data [:debug :toc :render-toc-link] entry)
  [:div {:style {:margin-top "10px"}}
   [:a
    {:style    {:margin-left "10px"
                :font-size   "1.3em"
                :font-weight "bold"}
     :href     "#"
     :on-click #(re-frame/dispatch [:toc/navigate-fx entry])}
    (:display entry)]
   [:hr]
   ])


(defn toc-panel []
  (let [toc-panel (re-frame/subscribe [:toc-panel])
        font-size (re-frame/subscribe [:font-size])]
    [:div.toc-panel.hidden-xs.col-sm-3-12.col-md-3-12.col-lg-3-12
     {:style {:border       "solid 2px black"
              :border-right "0"
              :max-width    "40ch"
              :font-size    (str @font-size "px")}}
     [:div
      ^{:key (str ":toc-panel/" (:index (:toc-header @toc-panel)))}
      [render-toc-title (:toc-header @toc-panel)]]
     [:ul

      (doall
        (for [entry (:toc-entries @toc-panel)]
          ^{:key (str ":toc-panel/" (:index entry))}
          [render-toc-entry entry]))]]))


;;;;;;;;;;;;;;;;;;;
;; Reading Frame ;;
;;;;;;;;;;;;;;;;;;;

(defn child-page [child file]
  [:div
   [:hr]
   [:div [:h3 (:display child)]
    (if file
      [:div {:dangerouslySetInnerHTML {:__html (markdown/md->html (:markdown file)
                                                                  :heading-anchors true
                                                                  :reference-links? true)}}]
      [:div "No File!"])]
   [:br]])

(defn capitalize [text state]
  (println text)
  (if (re-find #"href" text)
    (do
      (println (clojure.string/split text #"href"))
      (println "text - " text)
      ;(println "state - " state)
      ))
  [(.toUpperCase text) state])

(defn printer [text state]
  ;(println text)
  ; (println state)
  (if (re-find #"href" text)
    [(clojure.string/replace text #"href='" "href='#") state]
    [text state]
    ))


(defn reading-panel
  "Render the reading frame"
  []
  (let [reading-panel (re-frame/subscribe [:reading-panel])
        github-files (re-frame/subscribe [:github-files])
        font-size (re-frame/subscribe [:font-size])]
    [:div.reading-panel.col-xs-12-12.col-sm-9-12.col-md-9-12.col-lg-9-12
     ;; TODO - Fix width of TOC when growing/shrinking - too much whitespace on the right, that should shrink first
     [:div
      {:style {:padding-left  "10px"
               :border        "solid 2px black"
               :max-width     "80ch"
               :font-size     (str @font-size "px")
               :margin-right  "10px"
               :padding-right "10px"}}

      [:div.frow.justify-end.items-stretch
       [:img.clickable {:src    "icons/ic_menu_black_24px.svg"
                        :height "24px"}]
       [:img.clickable {:src      "icons/ic_zoom_out_black_24px.svg"
                        :height   "24px"
                        :on-click #(re-frame/dispatch [:decrease-font-size])}]
       [:img.clickable {:src      "icons/ic_zoom_in_black_24px.svg"
                        :height   "24px"
                        :on-click #(re-frame/dispatch [:increase-font-size])}]]
      [:div

       {:style                   {:margin-top "-15px"}

        ;:dangerouslySetInnerHTML {:__html (:markdown @reading-panel)}
        :dangerouslySetInnerHTML {:__html (markdown/md->html (:markdown @reading-panel)
                                                             ;:heading-anchors true
                                                             ;:reference-links? true
                                                             :custom-transformers [printer]
                                                             ;:replacement-transformers (cons custom-url-transformer transformer-vector)
                                                             )}
        }]
      ;(doall (for [child (:children @reading-panel)]
      ;         ^{:key (str ":reading-panel/child-" (:index child))}
      ;         [child-page child ((:path child) @github-files)]))
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
        [reading-panel]]])))