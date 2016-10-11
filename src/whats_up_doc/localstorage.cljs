(ns whats-up-doc.localstorage
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]))


(defn localstorage-key [user repo path]
  (str "whats-up-doc-" user "/" repo "/" path))


;(re-frame/reg-cofx
;  :localstorage
;  (fn [cofx args]
;    (re-frisk/add-in-data [:debug :localstorage/localstorage-cofx] {:cofx cofx :args args})
;    (assoc cofx :localstorage {:args args})))


;; Behaviour for localstorage
;; -


(def github-cache
  (re-frame/->interceptor
    :id :localstorage/github-cache
    :before (fn [context]
              (re-frisk/add-in-data [:debug :localstorage :localstorage/github-cache :before] {:context context})
              (let [url (:url (val (get-in context [:coeffects :event])))]

                )
              context)))



(def intercept-key
  (re-frame/->interceptor
    :id :localstorage/intercept-key
    :before (fn [context]
              (re-frisk/add-in-data [:debug :localstorage :localstorage/intercept-key :before] {:context context})
              (assoc-in context [:coeffects :localstorage]
                        {:key (:url (val (get-in context [:coeffects :event])))
                         }))))


(def int1
  (re-frame/->interceptor
    :id :int1
    :before (fn [context]


              (re-frisk/add-in-data [:debug :localstorage/int1 :before] {:context context})


              context)
    :after (fn [context]
             (re-frisk/add-in-data [:debug :localstorage/int1 :after] {:context context})
             context)))


(def int2
  (re-frame/->interceptor
    :id :int2
    :before (fn [context]
              (re-frisk/add-in-data [:debug :localstorage/int2 :before] {:context context})
              context)
    :after (fn [context]
             (re-frisk/add-in-data [:debug :localstorage/int2 :after] {:context context})
             context)))


;(.getItem js/localStorage "whats-up-doc/https://api.github.com/repos/Day8/re-frame/contents/docs?ref=develop")
;
;(.setItem js/localStorage "https://api.github.com/repos/Day8/re-frame/contents/docs?ref=develop" )
;
;
;
;
;;; -- Local Storage  ----------------------------------------------------------
;;;
;;; Part of the todomvc challenge is to store todos in LocalStorage, and
;;; on app startup, reload the todos from when the program was last run.
;;; But we are not to load the setting for the "showing" filter. Just the todos.
;;;
;
;(def ls-key "todos-reframe")                                ;; localstore key
;
;;(.getItem js/localStorage "whats-up-doc/https://api.github.com/repos/Day8/re-frame/contents/docs?ref=develop")
;
;(defn todos->local-store
;  "Puts todos into localStorage"
;  [todos]
;  (.setItem js/localStorage ls-key (str todos)))            ;; sorted-map writen as an EDN map
;
;(re-frame/reg-cofx
;  :local-store-todos
;  (fn [cofx _]
;    "Read in todos from localstore, and process into a map we can merge into app-db."
;    (assoc cofx :local-store-todos
;                (into (sorted-map)
;                      (some->> (.getItem js/localStorage ls-key)
;                               (cljs.reader/read-string)       ;; stored as an EDN map.
;                               )))))
