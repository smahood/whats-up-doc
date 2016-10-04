(ns whats-up-doc.events
  (:require [re-frame.core :as re-frame]
            [whats-up-doc.db :as db]
            [whats-up-doc.github-fx]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize Application Data ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
  :initialize
  (fn [{:keys [db]} [_ options]]
    {:db          (if (empty? db)
                    (merge db db/initial-state {:initialization-options options})
                    db)
     :github/root (:root options)}))

;;;;;;;;;;;;;;;;;;;;
;; Error Handling ;;
;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-db
  :display-error
  (fn [db event]
    ;; TODO - probably change this to an -fx handler
    ;; TODO - change error display to something nicer
    (println "Error: " event)))


;;;;;;;;;;;;;;;;;;;;;
;; Github Fetching ;;
;;;;;;;;;;;;;;;;;;;;;


;; TODO - Any place for an interceptor here?
;; TODO - Use event-fx instead of event-db or something else?
;; TODO - when files are downloaded, put them into local storage or similar. Then
;; when contents are loaded next time, check against the SHA and load the local
;; copy if they are equal

;
;(re-frame/reg-event-db
;  :fetch-github-file-success
;  (println ":fetch-github-file-success")
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

;;;;;;;;;;;;;;;;;;;;;;;
;; Navigation Events ;;
;;;;;;;;;;;;;;;;;;;;;;;

;
;(re-frame/reg-event-db
;  :navigation-link-clicked
;  (fn [db [_ data parent]]
;    (let [s (:url parent)
;          match (first (rest (clojure.string/split (:path parent) "/")))
;          replacement (:link data)
;          url (clojure.string/replace s match replacement)]
;      (re-frame/dispatch [:github/fetch-file-fx url]))
;    db))

(re-frame/reg-event-fx
  :navigation-link-clicked
  (fn [{:keys [db]} [_ child parent]]
    ;(println ":navigation-link-clicked child" child)
    ;(println ":navigation-link-clicked parent" parent)

    {:github/file (:url child)}))


;;;;;;;;;;;;;;;;;;;;;;;;;
;; Changing font sizes ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-db
  :increase-font-size
  (fn [db _]
    (assoc db :font-size (+ (:font-size db) 1))))


(re-frame/reg-event-db
  :decrease-font-size
  (fn [db _]
    (assoc db :font-size (- (:font-size db) 1))))


