(ns whats-up-doc.github-fx
  "This namespace contains all the functions required to transform github API results."
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]
            [camel-snake-kebab.core :as kebab]
            ))

(defn get-folder-from-file [url]
  (clojure.string/join "/" (drop-last (clojure.string/split url #"/"))))

(defn get-folder-keyword [url]
  (keyword (last (clojure.string/split "https://api.github.com/repos/Day8/re-frame/contents/docs" #"/contents/"))))

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


(defn transform-file-result
  "Transform the result returned from the github API for individual files"
  [result]
  (let [markdown (decode-markdown result)
        toc-data (build-toc-data result markdown)]
    (if (= (:type result) "file")
      (assoc
        ;(select-keys result [:name :path :type :size :sha :url])
        result
        :markdown markdown
        :toc-data toc-data)
      ;; TODO - proper error message and error handling
      {(keyword (:path result)) (str "Error: Result type is " (:type result) ", expecting file")})))

(defn transform-folder-result
  [folder result]
  (let [files (filter #(= "file" (:type %)) result)
        folders (filter #(= "dir" (:type %)) result)]

    {:url     folder
     :files   files
     :folders folders
     ;:file-count (count files)
     ;:folder-count (count folders)
     ;:file-size (reduce + (map :size files)

     }))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fetch root document ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-fx
  :github/root
  (fn [options]
    (re-frame/dispatch [:github/fetch-root-fx options])
    {}))


(re-frame/reg-event-fx
  :github/fetch-root-fx
  (fn [{:keys [db]} [_ root]]
    (re-frisk/add-in-data [:debug :github :github/fetch-root-fx] {:db            db
                                                                  :root          root
                                                                  :github-folder (get-folder-from-file root)
                                                                  })
    {:http-xhrio    {:method          :get
                     :uri             root
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:github/fetch-root-success root]
                     :on-failure      [:github/fetch-root-failure root]}
     :github/folder (get-folder-from-file root)}))


(re-frame/reg-event-db
  :github/fetch-root-success
  (fn [db [_ root result]]
    (re-frisk/add-in-data [:debug :github :github/fetch-root-success] {:db db :root root :result result})
    (let [transformed-result (transform-file-result result)]
      (-> db
          (assoc-in [:github-files (keyword (:path result))] transformed-result)
          (assoc :toc-panel (:toc-data transformed-result))))))


(re-frame/reg-event-fx
  :github/fetch-root-failure
  (fn [db [_ root result]]
    (re-frisk/add-in-data [:debug :github :github/fetch-root-failure] {:db db :root root :result result})
    (re-frame/dispatch [:display-error result])             ;; TODO - Make this error SUPER obvious, since things can't work without it
    {}))


;;;;;;;;;;;;;;;;;
;; Fetch files ;;
;;;;;;;;;;;;;;;;;


;; When an internal link is clicked, do the following (or some approximation)
;; 1) Check to see if file exists in the db
;; 2) If results exist in DB, then check to see if the SHA is the same (should be pre-loaded from other actions)
;; 3) If SHA doesn't match or doesn't exist, then re-fetch the document
;; 4) Render the internal-link
;; TODO - Decide when and how to load folder contents

(re-frame/reg-fx
  :github/file
  (fn [file]
    (re-frisk/add-in-data [:debug :github :github/file] {:file file})
    (re-frame/dispatch [:github/fetch-file-fx file])))


(re-frame/reg-event-fx
  :github/fetch-file-fx
  (fn [{:keys [db]} [_ file]]
    (re-frisk/add-in-data [:debug :github :github/fetch-file-fx] {:db db :file file})
    ;; TODO - check if this currently exists in db and if SHA is up to date with folder. If so, don't have to fetch it
    {:http-xhrio {:method          :get
                  :uri             file
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:github/fetch-file-success file]
                  :on-failure      [:github/fetch-folder-failure file]}}))


(re-frame/reg-event-db
  :github/fetch-file-success
  (fn [db [_ file result]]
    (re-frisk/add-in-data [:debug :github :github/fetch-file-success] {:db db :file file :result result})
    (assoc-in db
              [:github-files (keyword (:path result))]
              (transform-file-result result))))


(re-frame/reg-event-fx
  :github/fetch-file-failure
  (fn [db [_ file result]]
    (re-frisk/add-in-data [:debug :github :github/fetch-file-failure] {:db db :file file :result result})
    (re-frame/dispatch [:display-error result])
    {}))


;;;;;;;;;;;;;;;;;;;
;; Fetch folders ;;
;;;;;;;;;;;;;;;;;;;


(re-frame/reg-fx
  :github/folder
  (fn [folder]
    (re-frisk/add-in-data [:debug :github :github/folder] {:folder folder})
    (re-frame/dispatch [:github/fetch-folder-fx folder])))


(re-frame/reg-event-fx
  :github/fetch-folder-fx
  (fn [{:keys [db]} [_ folder]]
    (re-frisk/add-in-data [:debug :github :github/fetch-folder-fx] {:db db :folder folder})
    {:http-xhrio {:method          :get
                  :uri             folder
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:github/fetch-folder-success folder]
                  :on-failure      [:github/fetch-folder-failure folder]}}))


(re-frame/reg-event-db
  :github/fetch-folder-success
  (fn [db [_ folder result]]
    (re-frisk/add-in-data [:debug :github :github/fetch-folder-success] {:db db :folder folder :result result})
    ;; TODO - should I be checking stale files whenever a folder is loaded?
    (assoc-in db [:github-folders (get-folder-keyword folder)]
              (transform-folder-result folder result))))


(re-frame/reg-event-fx
  :github/fetch-folder-failure
  (fn [db [_ folder result]]
    (re-frisk/add-in-data [:debug :github :github/fetch-folder-failure] {:db db :folder folder :result result})
    (re-frame/dispatch [:display-error result])
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
