(ns whats-up-doc.github
  "This namespace contains all the functions required to transform github API results."
  (:require [camel-snake-kebab.core :as kebab]))


(defn base64-decode
  "Decode the base64 string that is returned by the github API"
  [x]
  (js/atob (clojure.string/replace x #"\s" "")))

(defn extract-toc-base-data
  "Extracts links and headings"
  [markdown-content]
  (re-seq #"#{1,6}.+|\[.*\]\(.*\)" markdown-content))

(defn link-url
  "Get the full URL for a partial link"
  [parent child]
  (let [s (:url parent)
        match (first (rest (clojure.string/split (:path parent) "/")))
        replacement child]
    (clojure.string/replace s match replacement)))



(defn build-single-file-toc-data
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
           :link     link})
        (re-matches #"\[.*\]\(.*\)" x)
        (let [title-str (re-find #"\[.*\]" x)
              link-str (re-find #"\(.*\)" x)
              link (subs link-str 1 (- (count link-str) 1))]
          {:type     "link"
           :markdown x
           :display  (subs title-str 1 (- (count title-str) 1))
           :link     link
           :url      (link-url parent link)})))))





(defn transform-api-result
  "Transform the result returned from the github API"
  [result]
  (let [decoded-markdown (if (= (:encoding result) "base64")
                           (base64-decode (:content result))
                           (str "Error: Result is decoded with " (:encoding result)
                                ", expecting base64"))
        toc-data (build-single-file-toc-data result decoded-markdown)]

    (if (= (:type result) "file")
      (assoc
        (select-keys result [:name :path :type :size :sha :url])
        :markdown decoded-markdown
        :toc-data toc-data)

      ;{:name     (:name result)
      ;                          :type     (:type result)
      ;                          :markdown markdown
      ;                          :size     (:size result)
      ;                          :sha      (:sha result)
      ;                          :url      (:url result)
      ;                          :children children}}
      {(keyword (:path result)) (str "Error: Result type is " (:type result) ", expecting file")})))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; For REPL and development use only        ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def example-result
  (get-in @re-frame.db/app-db [:github-files :docs/README.md]))


(transform-api-result example-result)

(:toc-data (transform-api-result example-result))
