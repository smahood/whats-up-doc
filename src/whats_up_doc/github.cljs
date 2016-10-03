(ns whats-up-doc.github
  "This namespace contains all the functions required to transform github API results."
  (:require [camel-snake-kebab.core :as kebab]))


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
           :child-data {}   ;; TODO - can I store the child data here? Is there a point?
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