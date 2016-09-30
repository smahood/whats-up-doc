(ns whats-up-doc.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [camel-snake-kebab.core :as kebab]
            [re-frisk.core :refer-macros [def-view]]
            ))


(defn toc-react-key
  "Generate key for react based on parent, type, and link.
   Hopefully this will be unique, but some sort of uniqueness check
   needs to be built in to the parser.
  "
  [parent type link]
  (str parent "-" type "-" link))


;; Top Menu

(defn top-menu []
  [:div.frow.justify-start.gutters.items-stretch
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


;; Table of Contents

;(declare render-toc-item)
;(declare render-file-toc)

;
;(defn render-toc-section [items]
;  [:ul {:style {:margin-left "10px"}}
;   (for [item items]
;     [render-toc-item item])])
;
;
;(defn render-toc-item [x]
;  (if (vector? x)
;    [:li [render-toc-section x]]
;    [:li [:a {:href (str "#-" (kebab/->kebab-case x))} x]]))


(defn render-heading
  "Renders markdown headings"
  [data parent]
  ;^{:key (str (:path parent) "-" (:link data))}
  [:li [:span {:style {:font-weight "bold"}} (:display data)]])

(defn render-link
  "Render markdown links"
  [data parent]
  ;^{:key (str (:path parent) "-" (:link data))}
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

(declare render-toc-item)


(defn render-visited-toc [file]
  [:ul.nested
   (for [x (:toc-data file)]
     [render-toc-item x file nil])])
     ;^{:key (toc-react-key (:path file) (:type x) (:link x))}
     ;[:li (toc-react-key (:path file) (:type x) (:link x))])])




;(for [x (:toc-data file-data)]
;              (let [react-key (str (:path file-data) "-" (:type x) "-" (:link x))]
;                (cond
;                  (= (:type x) "heading") ^{:key react-key} [render-heading x file-data]
;                  (and (= (:type x) "link") (nil? (distinct-filenames (:link x))))
;                  ^{:key react-key} [render-link x file-data]
;                  (and (= (:type x) "link") (distinct-filenames (:link x)))
;                  ^{:key react-key} [render-visited-link x file-data])))]))


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
       {:src   "icons/ic_expand_more_black_24px.svg"
        :style {:height      "16px"
                :margin-left "20px"}}]]
     [render-visited-toc file]]))


(defn render-toc-item [item parent visited-link?]
  (let [react-key (toc-react-key (:path parent) (:type item) (:link item))]
    (cond
      (= (:type item) "heading") ^{:key react-key} [render-heading item parent]
      (and (= (:type item) "link") (nil? visited-link?))
      ^{:key react-key} [render-link item parent]
      (and (= (:type item) "link") visited-link?)
      ^{:key react-key} [render-visited-link item parent])))


(def-view render-file-toc [file-data]
          (let [github-files (re-frame/subscribe [:github-files])
                distinct-filenames (set (map #(:name (val %)) @github-files))]
            [:ul
             [:li "File TOC"]
             (for [x (:toc-data file-data)]
               (let [visited-link? (distinct-filenames (:link x))]
                 [render-toc-item x file-data visited-link?]))]))


(defn render-full-toc [file-data]
  ;(println file-data)
  (if file-data
    [render-file-toc file-data]
    [:li "No file data"]))


(defn table-of-contents [root-key]
  (let [toc (re-frame/subscribe [:table-of-contents])
        github-files (re-frame/subscribe [:github-files])]
    [:ul
     [:li (str root-key)]
     [render-full-toc (root-key @github-files)]

     ;(for [item @toc]
     ;  ^{:key item} [render-toc-item item]
     ;  )

     ]))



(defn reading-frame2 []
  [:div "test"])

(defn reading-frame []
  (let [github-content (re-frame/subscribe [:reading-frame-content])
        font-size (re-frame/subscribe [:font-size])]





    [:div.main-content.col-xs-12-12.col-sm-9-12.col-md-9-12.col-lg-8-12


     ;           [:div (str "Github Content" (:decoded-content (:README.md @github-content)))]


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

      [:div
       [:p "This text is 81 characters wide. This text is 80 characters wide. This text is 8"]
       [:p "This text is 70 characters wide. This text is 70 characters wide. This"]
       [:p "This text is 60 characters wide. This text is 60 characterss"]
       [:p "his, milord, is my family’s axe. We have owned it for almost nine hundred years, see. Of course, sometimes it needed a new blade. And sometimes it has required a new handle, new designs on the metalwork, a little refreshing of the ornamentation . . . but is this not the nine hundred-year-old axe of my family? And because it has changed gently over time, it is still a pretty good axe, y’know. Pretty good."]

       [:p "This text is 80 characters wide. This text is 80 characters wide. This text is 8"]
       [:p "This text is 70 characters wide. This text is 70 characters wide. This"]
       [:p "This text is 60 characters wide. This text is 60 characterss"]
       [:p "his, milord, is my family’s axe. We have owned it for almost nine hundred years, see. Of course, sometimes it needed a new blade. And sometimes it has required a new handle, new designs on the metalwork, a little refreshing of the ornamentation . . . but is this not the nine hundred-year-old axe of my family? And because it has changed gently over time, it is still a pretty good axe, y’know. Pretty good."]
       [:p "This text is 80 characters wide. This text is 80 characters wide. This text is 8"]
       [:p "This text is 70 characters wide. This text is 70 characters wide. This"]
       [:p "This text is 60 characters wide. This text is 60 characterss"]
       [:p "his, milord, is my family’s axe. We have owned it for almost nine hundred years, see. Of course, sometimes it needed a new blade. And sometimes it has required a new handle, new designs on the metalwork, a little refreshing of the ornamentation . . . but is this not the nine hundred-year-old axe of my family? And because it has changed gently over time, it is still a pretty good axe, y’know. Pretty good."]
       [:p "This text is 80 characters wide. This text is 80 characters wide. This text is 8"]
       [:p "This text is 70 characters wide. This text is 70 characters wide. This"]
       [:p "This text is 60 characters wide. This text is 60 characterss"]
       [:p "his, milord, is my family’s axe. We have owned it for almost nine hundred years, see. Of course, sometimes it needed a new blade. And sometimes it has required a new handle, new designs on the metalwork, a little refreshing of the ornamentation . . . but is this not the nine hundred-year-old axe of my family? And because it has changed gently over time, it is still a pretty good axe, y’know. Pretty good."]
       [:p "This text is 80 characters wide. This text is 80 characters wide. This text is 8"]
       [:p "This text is 70 characters wide. This text is 70 characters wide. This"]
       [:p "This text is 60 characters wide. This text is 60 characterss"]
       [:p [:a {:id "#-what-problem-does-it-solve?"}]
        "his, milord, is my family’s axe. We have owned it for almost nine hundred years, see. Of course, sometimes it needed a new blade. And sometimes it has required a new handle, new designs on the metalwork, a little refreshing of the ornamentation . . . but is this not the nine hundred-year-old axe of my family? And because it has changed gently over time, it is still a pretty good axe, y’know. Pretty good."]
       ]]]

    ))

;; Main

(defn main []
  [:div
   [top-menu]

   [:div.frow.justify-start.items-stretch

    [:div.nav-sidebar.hidden-xs.col-sm-3-12.col-md-3-12.col-lg-3-12
     {:style {:border "solid 2px black"}}
     [table-of-contents :docs/README.md]
     ]
    [reading-frame]
    ]])
