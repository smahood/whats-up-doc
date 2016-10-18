(ns whats-up-doc.github-fx
  "This namespace contains all the functions required to transform github API results."
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]
            [camel-snake-kebab.core :as kebab]
            [whats-up-doc.markdown-fx :as markdown]
            [cognitect.transit :as transit]))


(defn get-folder-from-file [url]
  (clojure.string/join "/" (drop-last (clojure.string/split url #"/"))))


(defn get-branch-from-file [url]
  (let [vec (clojure.string/split url "?")
        branch (if (< 1 (count vec)) (str "?" (last vec)) "")]
    branch))


(defn get-folder-keyword [url]
  (keyword (last (clojure.string/split url #"/contents/"))))


(defn get-folder-keyword-from-file [url]
  (get-folder-keyword (str (get-folder-from-file url) (get-branch-from-file url))))


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
  (re-frisk/add-in-data [:debug :github :github/build-toc-data] {:parent           parent
                                                                 :markdown-content markdown-content})
  (into []
        (let [extracted-strings (extract-toc-base-data markdown-content)]
          (for [x extracted-strings]
            (let [index (+ 1 (.indexOf extracted-strings x))]
              (cond
                (re-matches #"#{1,6}.+" x)
                (let [display (clojure.string/trim (clojure.string/replace x #"#{1,6}" ""))
                      link (kebab/->kebab-case display)]
                  {:type     "heading"
                   :markdown x
                   :display  display
                   :link     link
                   :expanded true
                   :index    index
                   })
                (re-matches #"\[.*\]\(.*\)" x)
                (let [title-str (re-find #"\[.*\]" x)
                      link-str (re-find #"\(.*\)" x)
                      link (subs link-str 1 (- (count link-str) 1))
                      folder (get-folder-from-file (:path parent))
                      path (keyword (clojure.string/join "/" [folder link]))]
                  {:type       "link"
                   :markdown   x
                   :display    (subs title-str 1 (- (count title-str) 1))
                   :link       link
                   :url        (build-child-url parent link)
                   :child-data {}                           ;; TODO - can I store the child data here? Is there a point?
                   :index      index
                   :folder     folder
                   :path       path
                   :expanded   false})))))))

(defn build-toc-header
  [transformed-file]
  {:type     "link"
   :markdown (str "[Table of Contents](" (:name transformed-file) ")")
   :display  "Table of Contents"
   :link     (:name transformed-file)
   :url      (:url transformed-file)
   :path     (keyword (:path transformed-file))
   :index    0})

(defn transform-file-result
  "Transform the result returned from the github API for individual files"
  [result]
  (re-frisk/add-in-data [:debug :github :github/transform-file-result] {:result result})
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

    {:url          folder
     :files        files
     :folders      folders
     :file-count   (count files)
     :folder-count (count folders)
     :file-size    (reduce + (map :size files))}))


(defn inline-reading-panel []
  ;; TODO - put together data for inline reading panel
  ;; Order of content should be based on the ToC
  ;; - Title of First Page
  ;; --- Content of Page
  ;; ---------------------
  ;; - Title of Second Page
  ;; --- Content of Second Page
  ""
  )
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Caching in LocalStorage ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; ;; Unused - keeping it here until caching is for sure working well
;(def read-cache
;  (re-frame/->interceptor
;    :id :github/read-cache
;    :before (fn [context]
;              (re-frisk/add-in-data [:debug :github :github/read-cache :before] {:context context})
;              (let [root (-> context :coeffects :db :root :url)
;                    cache (.getItem js/localStorage root)]
;
;                (assoc-in context [:coeffects :cache] cache)))
;    :after (fn [context]
;             (re-frisk/add-in-data [:debug :github :github/read-cache :after] {:context context})
;             context)))


(def write-cache
  (re-frame/->interceptor
    :id :github/write-cache
    :before (fn [context]
              (re-frisk/add-in-data [:debug :github :github/write-cache :before] {:context context})
              context)
    :after (fn [context]
             (let [key (get-in context [:effects :db :root :url])
                   cache {:root          (get-in context [:effects :db :root])
                          :github-files  (get-in context [:effects :db :github-files])
                          :toc-panel     (get-in context [:effects :db :toc-panel])
                          :reading-panel (get-in context [:effects :db :reading-panel])}]
               (re-frisk/add-in-data [:debug :github :github/write-cache :after] {:context       context
                                                                                  :cache         cache
                                                                                  :transit-cache (transit/write (transit/writer :json) cache)})
               (.setItem js/localStorage key (transit/write (transit/writer :json) cache))
               context))))




;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fetch root document ;;
;;;;;;;;;;;;;;;;;;;;;;;;;


(re-frame/reg-fx
  :github/root
  (fn [args]
    ;; TODO - SPEC - args should be a single root string
    ;; TODO - re-frisk doesn't seem to be working here
    (re-frisk/add-in-data [:debug :github :github/root] {:args args})
    (re-frame/dispatch [:github/fetch-root-fx args])
    {}))


(re-frame/reg-event-fx
  :github/fetch-root-fx
  (fn [{:keys [db]} [_ root]]
    (re-frisk/add-in-data [:debug :github :github/fetch-root-fx]
                          {:db            db
                           :root          root
                           :github-folder (get-folder-from-file root)})
    {:http-xhrio    {:method          :get
                     :uri             root
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:github/fetch-root-success root]
                     :on-failure      [:github/fetch-root-failure root]}
     :github/folder (str (get-folder-from-file root) (get-branch-from-file root))
     :db            (assoc db :initialized? true)}))


(re-frame/reg-event-db
  :github/fetch-root-success
  [write-cache]
  (fn [db [_ root result]]
    (let [transformed-result (transform-file-result result)
          toc-header (build-toc-header transformed-result)]
      (re-frisk/add-in-data [:debug :github :github/fetch-root-success] {:db                 db
                                                                         :root               root
                                                                         :result             result
                                                                         :transformed-result transformed-result})
      (-> db
          (assoc :root transformed-result)
          (assoc-in [:github-files (keyword (:path result))] transformed-result)
          (assoc-in [:toc-panel :toc-header] toc-header)
          (assoc-in [:toc-panel :toc-entries] (:toc-data transformed-result))
          ;(assoc-in [:reading-panel :name] (:name transformed-result))
          (assoc-in [:reading-panel :markdown] (:markdown transformed-result))
          ;(assoc-in [:reading-panel :children]
          ;          (filter #(= "link" (:type %)) (:toc-data transformed-result)))
          (assoc :initialized? true)))))


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
  (fn [[url key]]
    (re-frisk/add-in-data [:debug :github :github/file] {:key key
                                                         :url url})
    (re-frame/dispatch [:github/fetch-file-fx url key])))


(re-frame/reg-event-fx
  :github/fetch-file-fx
  (fn [{:keys [db]} [_ url key]]
    (let [cached-file (get-in db [:github-files key])
          folder-key (get-folder-keyword-from-file url)
          folder (get-in db [:github-folders folder-key])
          file-in-folder (first (filter #(= (:url cached-file) (:url %)) (:files folder)))]
      (re-frisk/add-in-data [:debug :github :github/fetch-file-fx] {:db             db
                                                                    :url            url
                                                                    :key            key
                                                                    :cached-file    cached-file
                                                                    :folder-key     folder-key
                                                                    :folder         folder
                                                                    :file-in-folder file-in-folder})
      (if (and cached-file (= (:sha cached-file) (:sha file-in-folder)))
        {:db (assoc-in db [:reading-panel :markdown] (:markdown cached-file))} ;; If cached file matches folder SHA, don't bother fetching it

        {:http-xhrio {:method          :get
                      :uri             url
                      :response-format (ajax/json-response-format {:keywords? true})
                      :on-success      [:github/fetch-file-success url]
                      :on-failure      [:github/fetch-folder-failure url]}}))))


(re-frame/reg-event-db
  :github/fetch-file-success
  [write-cache]
  (fn [db [_ file result]]
    (let [transformed-result (transform-file-result result)]
      (re-frisk/add-in-data [:debug :github :github/fetch-file-success] {:db                 db
                                                                         :file               file
                                                                         :result             result
                                                                         :transformed-result transformed-result})
      (-> db
          (assoc-in [:github-files (keyword (:path result))] (transform-file-result result))
          (assoc-in [:reading-panel :markdown] (:markdown transformed-result))))))


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
    (if (= "eager" (get-in db [:initialization-options :loading]))
      (doseq [file result]
        (cond (= "file" (:type file)) (re-frame/dispatch [:github/fetch-file-fx (:url file)])
              (= "dir" (:type file)) (re-frame/dispatch [:github/fetch-folder-fx (:url file)]))))
    ;; TODO - should I be checking stale files whenever a folder is loaded?
    (assoc-in db [:github-folders (get-folder-keyword folder)]
              (transform-folder-result folder result))))


(re-frame/reg-event-fx
  :github/fetch-folder-failure
  (fn [db [_ folder result]]
    (re-frisk/add-in-data [:debug :github :github/fetch-folder-failure] {:db db :folder folder :result result})
    (re-frame/dispatch [:display-error result])
    {}))