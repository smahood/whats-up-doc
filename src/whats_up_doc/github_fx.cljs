(ns whats-up-doc.github-fx
  "This namespace contains all the functions required to transform github API results."
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [camel-snake-kebab.core :as kebab]))


(defn base64-decode
  "Decode a base64 string"
  [x]
  (js/atob (clojure.string/replace x #"\s" "")))


(defn decode-markdown
  [result]
  (if (= (:encoding result) "base64")
    (base64-decode (:content result))
    ;; TODO - Proper error message and handling
    (str "Error: Result is decoded with " (:encoding result)
         ", expecting base64")))


(defn extract-toc-base-data
  "Extracts links and headings"
  ;; TODO - Determine if any other markdown will be used as part of TOC
  [markdown-content]
  (re-seq #"#{1,6}\s.+|\[.*\]\(.*\)" markdown-content))


(defn build-child-url
  "Get the full URL for a partial link"
  [parent child]
  (let [s (:url parent)
        match (first (rest (clojure.string/split (:path parent) "/")))
        replacement child]
    (clojure.string/replace s match replacement)))


(defn build-toc-data
  "Parses the markdown from a single file into a map that can be used to build the table of contents"
  [parent markdown-content]
  (let [extracted-strings (extract-toc-base-data markdown-content)]
    (for [x extracted-strings]
      (cond
        (re-matches #"#{1,6}.+" x)
        (let [display (clojure.string/trim (clojure.string/replace x #"#{1,6}" ""))
              link (kebab/->kebab-case display)]
          {:type     "heading"
           :markdown x
           :display  display
           :link     link
           :expanded true})
        (re-matches #"\[.*\]\(.*\)" x)
        (let [title-str (re-find #"\[.*\]" x)
              link-str (re-find #"\(.*\)" x)
              link (subs link-str 1 (- (count link-str) 1))]
          {:type       "link"
           :markdown   x
           :display    (subs title-str 1 (- (count title-str) 1))
           :link       link
           :url        (build-child-url parent link)
           :child-data {}                                   ;; TODO - can I store the child data here? Is there a point?
           :expanded   false})))))


(defn transform-api-result
  "Transform the result returned from the github API"
  [result]
  (let [markdown (decode-markdown result)
        toc-data (build-toc-data result markdown)]
    (if (= (:type result) "file")
      (assoc
        (select-keys result [:name :path :type :size :sha :url])
        :markdown markdown
        :toc-data toc-data)
      ;; TODO - proper error message and error handling
      {(keyword (:path result)) (str "Error: Result type is " (:type result) ", expecting file")})))

(re-frame/reg-event-fx :fetch-root
                       (fn [cofx event]
                         (println cofx)
                         (println event)

                         )

                       )


(re-frame/reg-fx :github/fetch-root
                 (fn [root]
                   (println ":github/fetch-root: " root)
                   (re-frame/dispatch )

                   {:http-xhrio {:method          :get
                                  :uri             root
                                  :response-format (ajax/json-response-format {:keywords? true})
                                  :on-success      [:fetch-root-success]
                                  :on-failure      [:fetch-root-failure]}}))

;; TODO - fetch-root should be defined by reg-event-fx
;; TODO - Define reg-fx :doc-root or something like that to prepare the root doc




(re-frame/reg-event-fx
  :fetch-github-file
  (fn [{:keys [db]} [_ uri]]
    {:http-xhrio {:method          :get
                  :uri             uri
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:fetch-github-file-success]
                  :on-failure      [:fetch-github-file-failure]}}))


(re-frame/reg-event-db
  :fetch-root-success
  (fn [db result]
    (println ":fetch-root-success " db result)
    (assoc-in db
              [:github-files (keyword (:path result))]
              (transform-api-result result))))


(re-frame/reg-event-db
  :fetch-root-failure
  (fn [db _]
    ;; TODO - proper error messages, error handling, etc etc
    (println "Failure when fetching github file")
    db))


(re-frame/reg-fx :github/fetch-folder
                 (fn [cofx]
                   {}))


(re-frame/reg-fx :github/fetch-file
                 (fn [cofx]
                   {}))





;(re-frame/reg-event-db
;  :fetch-github-file-success
;  (fn [db [_ result key]]
;    (assoc-in db
;              [:github-files (keyword (:path result))]
;              (github-api/transform-api-result result))))
;
;
;(re-frame/reg-event-db
;  :fetch-github-file-failure
;  (fn [db _]
;    ;; TODO - proper error messages, error handling, etc etc
;    (println "Failure when fetching github file")
;    db))
;
;
;(re-frame/reg-event-fx
;  :fetch-github-file
;  (fn [{:keys [db]} [_ uri]]
;    {:http-xhrio {:method          :get
;                  :uri             uri
;                  :response-format (ajax/json-response-format {:keywords? true})
;                  :on-success      [:fetch-github-file-success]
;                  :on-failure      [:fetch-github-file-failure]}}))
;
