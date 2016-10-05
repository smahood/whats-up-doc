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