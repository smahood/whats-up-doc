(ns whats-up-doc.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer-macros [def-view]]
            [camel-snake-kebab.core :as kebab]
            [markdown.core :as markdown]))

;;;;;;;;;;;;;;
;; Top Menu ;;
;;;;;;;;;;;;;;

(defn top-menu []
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

(declare render-toc-item)


(declare render-file-toc)


(defn toc-react-key
  "Generate key for react based on parent, type, and link.
   Hopefully this will be unique, but some sort of uniqueness check
   needs to be built in to the parser.
  "
  [parent type link]
  (str parent "-" type "-" link))


(defn render-heading
  "Renders markdown headings"
  [data parent]
  [:li [:span {:style {:font-weight "bold"}} (:display data)]])


(defn render-link
  "Render markdown links"
  [data parent]
  [:ul.nested
   [:li
    [:a
     {:href     "#"
      :on-click #(re-frame/dispatch [:navigation-link-clicked data parent])}
     (:display data)]
    [:img.clickable
     {:src   "icons/ic_expand_more_black_24px.svg"
      :style {:height      "16px"
              :margin-left "20px"}}]]])


(defn render-visited-toc [file]
  [:ul.nested
   (for [x (:toc-data file)]
     ^{:key (toc-react-key (:path file) (:type x) (:link x))} [render-toc-item x file nil])])


(defn render-visited-link
  "Render a link once it has been visited"
  [data parent]
  (let [github-files (re-frame/subscribe [:github-files])
        file (val (first (filter #(= (:url data) (:url (val %))) @github-files)))]
    [:ul.nested
     [:li
      [:a
       {:href     "#"
        :on-click #(re-frame/dispatch [:navigation-link-clicked data parent])}
       (str (:display data))]
      [:img.clickable
       {:src   "icons/ic_expand_less_black_24px.svg"
        :style {:height      "16px"
                :margin-left "20px"}}]]
     [render-visited-toc file]
     ;; TODO - make it possible to call the same render-file-toc for visited links
     ;[render-file-toc file]
     ]))

;; TODO - When link is loaded from root doc, add 2 more data keys - expanded (default false)
;; and loaded (or data)
;; When link is clicked, if the data is loaded then expand it. If data is not loaded, fetch the data
;; then expand the link.



(defn render-toc-item [item parent visited-link?]
  (cond
    (= (:type item) "heading") [render-heading item parent]
    (and (= (:type item) "link") (nil? visited-link?))
    [render-link item parent]
    ;(and (= (:type item) "link") visited-link?)
    ;[render-visited-link item parent]
    ))


(defn render-file-toc [file-data]
  (let [github-files (re-frame/subscribe [:github-files])
        distinct-filenames (set (map #(:name (val %)) @github-files))]
    [:ul
     [:li "File TOC"]
     (for [x (:toc-data file-data)]
       (let [visited-link? (distinct-filenames (:link x))
             react-key (toc-react-key (:path file-data) (:type x) (:link x))]
         ^{:key react-key} [render-toc-item x file-data visited-link?]))]))


(defn render-full-toc [file-data]
  (if file-data
    [render-file-toc file-data]
    [:li "No file data"]))


(defn table-of-contents-old [root-key]
  (let [github-files (re-frame/subscribe [:github-files])]
    (println (root-key @github-files))
    [:ul
     [:li (str root-key)]
     [render-full-toc (root-key @github-files)]]))



(defn render-toc-heading
  "Renders markdown headings"
  [data parent]
  [:li [:span {:style {:font-weight "bold"}} (:display data)]])




(defn render-toc-entry [entry]
  (println entry)
  (cond
    (= (:type entry) "heading") [:li "Heading - " (str entry)]
    (= (:type entry) "link") [:li "Link - " (str entry)]
    (= [:type entry] "") [:li "No Type" (str entry)]
    :else [:li "Other Type - " (:type entry) (str entry)])
  )




(defn table-of-contents []
  (let [github-files (re-frame/subscribe [:github-files])
        toc-panel (re-frame/subscribe [:toc-panel])]
    [:ul
     (doall
       (for [entry @toc-panel]
         ^{:key (str ":toc-panel/" (.indexOf @toc-panel entry))}
         [render-toc-entry entry]))]))

;;;;;;;;;;;;;;;;;;;
;; Reading Frame ;;
;;;;;;;;;;;;;;;;;;;


(defn render-reading-panel
  "Render the reading frame"
  []
  (let [github-files (re-frame/subscribe [:github-files])
        font-size (re-frame/subscribe [:font-size])
        toc-panel (re-frame/subscribe [:toc-panel])
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
      (for [file @github-files]
        ^{:key (str (key file))}
        [:div (str (key file))
         [:div {:dangerouslySetInnerHTML {:__html (markdown/md->html (:markdown (val file)))}}]
         [:br]
         [:hr]
         [:br]])]]))

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
     ;     [table-of-contents-old :docs/README.md]
     [table-of-contents]

     ]
    [render-reading-panel]]])
