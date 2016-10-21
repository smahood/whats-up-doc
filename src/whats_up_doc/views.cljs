(ns whats-up-doc.views
  (:require [reagent.core]
            [re-frame.core :as re-frame]
            [re-frisk.core :as re-frisk :refer-macros [def-view]]
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
    {:style {:font-weight "bold"}
     ;:on-click #(re-frame/dispatch [:reading/navigate-fx entry])
     }
    (:display entry)]])


(defn render-toc-link
  "Renders markdown links"
  [entry]
  (re-frisk/add-in-data [:debug :toc :toc/render-toc-link] entry)
  [:ul.nested
   [:li
    [:a
     {:href     (str "#" (:link entry))
      :on-click #(re-frame/dispatch [:toc/navigate-fx entry])
      }
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
    {:style {:margin-left "10px"
             :font-size   "1.3em"
             :font-weight "bold"}
     :href  "#"
     ;:on-click #(re-frame/dispatch [:toc/navigate-fx entry])
     }
    (:display entry)]
   [:hr]
   ])


(defn toc-panel []
  (let [toc-panel (re-frame/subscribe [:toc-panel])
        font-size (re-frame/subscribe [:font-size])]
    [:div.toc-panel.hidden-xs.col-sm-3-12.col-md-3-12.col-lg-3-12
     {:style {:border-right "solid 2px black"
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

(defn make-url
  [url-text]
  ;(println "href found: " url-text)
  (let [new-str (re-find #"href='.*?'" url-text)
        replace1 (clojure.string/replace new-str #"href='" "")
        replace2 (clojure.string/replace replace1 #"'" "")]
    ;(println "new-str: " new-str)
    ;(println "replace1: " replace1)
    ;(println "replace2: " replace2)
    ;(println (str "#" replace2))
    ;(println (clojure.string/replace url-text replace2 (str "#" replace2)))
    (clojure.string/replace url-text replace2 (str "#" replace2))))


(defn fix-url
  ; TODO - Deal with external 'something://' and '../' URLs
  ; check for url too
  [text state]
  (if (re-find #"href" text)
    [(make-url text) state]
    ;
    ;
    ;
    ;(do (println "href found")
    ;    (println text)
    ;    (let [new-str (re-find #"href='.*?'" text)
    ;          replace1 (clojure.string/replace new-str #"href='" "")
    ;          replace2 (clojure.string/replace replace1 #"'" "")]
    ;      ;(println new-str)
    ;      ;(println replace1)
    ;      (println replace2)
    ;      (println (str "#" replace2))
    ;
    ;
    ;      ;(println state)
    ;      [text state]
    ;      ))
    [text state]))

(defn fix-headings [text state]
  (cond (re-find #"<h1" text) (do (println "<h1 found")
                                  (println text)
                                  ;(println state)
                                  )
        (re-find #"<h2" text) (do (println "<h2 found")
                                  (println text)
                                  ;(println state)
                                  )
        (re-find #"<h3" text) (do (println "<h3 found")
                                  (println text)
                                  ;(println state)
                                  )
        (re-find #"<h4" text) (do (println "<h4 found")
                                  (println text)
                                  ;(println state)
                                  ))
  [text state])


(defn printer [text state]

  (if (re-find #"href" text)
    (do
      (println text)
      ; (println state)

      [(clojure.string/replace text #"href='" "href='#top#") state])
    [text state]
    ))
;
;(defn heading? [text type]
;  (when-not (every? #{\space} (take 4 text))
;    (let [trimmed (if text (clojure.string/trim text))]
;      (and (not-empty trimmed) (every? #{type} trimmed)))))
;
;(defn h1? [text]
;  (heading? text \=))
;
;(defn h2? [text]
;  (heading? text \-))
;
;(defn heading-text [text]
;  (-> (clojure.string/replace text #"^([ ]+)?[#]+" "")
;      (clojure.string/replace #"[#]+$" "")
;      clojure.string/trim))
;
;;
;
;(defn heading-level [text]
;  (let [num-hashes (count (filter #(not= \space %) (take-while #(or (= \# %) (= \space %)) (seq text))))]
;    (if (pos? num-hashes) num-hashes)))
;
;(defn make-heading [text heading-anchors]
;  (when-let [heading (heading-level text)]
;    (let [text (heading-text text)]
;      (str "<h" heading ">"
;           (if heading-anchors (str "<a name=\"" (-> text clojure.string/lower-case (clojure.string/replace " " "&#95;")) "\"></a>"))
;           text "</h" heading ">"))))
;
;(defn heading [text state]
;  (cond
;    (or (:codeblock state) (:code state))
;    [text state]
;
;    (h1? *next-line*)
;    [(str "<h1>" text "</h1>") (assoc state :heading true)]
;
;    (h2? *next-line*)
;    [(str "<h2>" text "</h2>") (assoc state :heading true)]
;
;    :else
;    (if-let [heading (make-heading text (:heading-anchors state))]
;      [heading (assoc state :inline-heading true)]
;      [text state])))
;


(re-frisk/def-view reading-panel
                   ; "Render the reading frame"
                   []
                   (let [reading-panel (re-frame/subscribe [:reading-panel])
                         github-files (re-frame/subscribe [:github-files])
                         font-size (re-frame/subscribe [:font-size])]
                     [:div.reading-panel.col-xs-12-12.col-sm-9-12.col-md-9-12.col-lg-9-12
                      ;; TODO - Fix width of TOC when growing/shrinking - too much whitespace on the right, that should shrink first
                      [:div
                       {:style {:padding-left  "10px"

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

                        {:style {:margin-top "-15px"}

                         ;:dangerouslySetInnerHTML {:__html (:markdown @reading-panel)}
                         :dangerouslySetInnerHTML
                                {:__html
                                 (markdown/md->html (:markdown @reading-panel)
                                                    ;:heading-anchors true
                                                    :reference-links? true

                                                    :custom-transformers [fix-url
                                                                          ;fix-headings
                                                                          ]
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

(re-frisk/def-view main []
                   (let [initialized? (re-frame/subscribe [:initialized?])]
                     (if @initialized?
                       [:div.main
                        [:div.frow.justify-start.items-stretch
                         {:style {:border "solid 2px black"
                                  :margin-bottom "10px"}}
                         [toc-panel]
                         [reading-panel]]])))