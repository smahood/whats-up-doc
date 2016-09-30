(ns whats-up-doc.events
  (:require [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [whats-up-doc.db :as db]
            [whats-up-doc.github :as github-api]))


;; Utility Functions

(defn base64-decode [x]
  (js/atob (clojure.string/replace x #"\s" "")))


;; Initialize Application Data

(re-frame/reg-event-db
  :initialize
  (fn [db _]
    (if (empty? db)
      (do
        ;(re-frame/dispatch [:fetch-github-file "https://api.github.com/repos/smahood/re-frame/contents/README.md?ref=master"])
        (re-frame/dispatch [:fetch-github-file "https://api.github.com/repos/Day8/re-frame/contents/docs/README.md?ref=master"])))
    (merge db db/initial-state)))


;; Changing font sizes
(re-frame/reg-event-db
  :increase-font-size
  (fn [db _]
    (assoc db :font-size (+ (:font-size db) 1))))

(re-frame/reg-event-db
  :decrease-font-size
  (fn [db _]
    (assoc db :font-size (- (:font-size db) 1))))


(defn fetch-all-pages [contents]
  (doall (for [page contents]
           (re-frame/dispatch [:fetch-github-file (:self (:_links page))]))))


;; Github Fetching


;; TODO - Make an interceptor, so when contents are fetched the result is intercepted
;; and it fires off new file fetch events
;; TODO - when files are downloaded, put them into local storage or similar. Then
;; when contents are loaded next time, check against the SHA and load the local
;; copy if they are equal


(defn render-markdown [markdown-content]

  )


(defn render-reading-content [content-keys]
  ;; expects vector of keys

  (for [key content-keys]
    ())

  )


(re-frame/reg-event-db
  :fetch-github-contents-success
  (fn [db [_ result]]
    (fetch-all-pages result)
    (assoc db :github-contents result)))



(re-frame/reg-event-fx
  :fetch-github-contents
  (fn [_ [_ uri]]
    {:http-xhrio {:method          :get
                  :uri             uri
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:fetch-github-contents-success]
                  :on-failure      (println "Failure in :fetch-github-contents")}}))


(re-frame/reg-event-db
  :fetch-github-contents-success
  (fn [db [_ result]]
    (fetch-all-pages result)
    (assoc db key result)))



(re-frame/reg-event-fx
  :fetch-github-contents
  (fn [_ [_ uri]]
    {:http-xhrio {:method          :get
                  :uri             uri
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:fetch-github-contents-success]
                  :on-failure      (println "Failure in :fetch-github-contents")}}))



(re-frame/reg-event-db
  :fetch-github-file-success
  (fn [db [_ result key]]
    (assoc-in db [:github-files (keyword (:path result))]
              (github-api/transform-api-result result))))


(re-frame/reg-event-fx
  :fetch-github-file
  (fn [{:keys [db]} [_ uri]]
    {:http-xhrio {:method          :get
                  :uri             uri
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:fetch-github-file-success]
                  :on-failure      (println "Failure in :fetch-github-file for " uri)}}))



(re-frame/reg-event-db
  :navigation-link-clicked
  (fn [db [_ data parent]]
    (let [s (:url parent)
          match (first (rest (clojure.string/split (:path parent) "/")))
          replacement (:link data)
          url (clojure.string/replace s match replacement)]
      (re-frame/dispatch [:fetch-github-file url]))
    db))