(ns whats-up-doc.events
  (:require [re-frame.core :as re-frame]
            [whats-up-doc.db :as db]
            [whats-up-doc.github-fx]))


(defn make-root [user repo path]
  (str "https://api.github.com/repos/" user "/" repo "/contents/" path))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Caching in LocalStorage ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-cache [root]
  (let [cache-string (.getItem js/localStorage root)
        cache (if cache-string (cljs.reader/read-string cache-string) {})]
    {:root          (or (:root cache) {})
     :toc-panel     (or (:toc-panel cache) {})
     :reading-panel (or (:reading-panel cache) {})
     :github-files  (or (:github-files cache) {})}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize Application Data ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(re-frame/reg-event-fx
  :initialize
  (fn [{:keys [db]} [_ options]]
    (let [root (make-root (:user options) (:repo options) (:path options))]
      {:db          (if (empty? db)
                      (merge
                        db
                        db/initial-state
                        {:initialization-options
                         (assoc options
                           :root root)}
                        (get-cache root))
                      db)
       :github/root root})))


;;;;;;;;;;;;;;;;;;;;
;; Error Handling ;;
;;;;;;;;;;;;;;;;;;;;


(re-frame/reg-event-db
  :display-error
  (fn [db event]
    ;; TODO - probably change this to an -fx handler
    ;; TODO - change error display to something nicer
    (println "Error: " event)))


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