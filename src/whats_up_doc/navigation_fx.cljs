(ns whats-up-doc.navigation-fx
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frisk.core :as re-frisk]
            [camel-snake-kebab.core :as kebab]
            ))


(defn build-child-url
  "Get the full URL for a partial link"
  [root partial-link]
  (let [s (:url root)
        match (first (rest (clojure.string/split (:path root) "/")))
        replacement partial-link
        result (clojure.string/replace s (str "docs/" match) replacement)
        ;index (map-indexed #{:k %1 :v %2} (clojure.string/split result "/"))
        ]
    ;(println "s: " s)
    ;(println "match: " match)
    ;(println "replacement: " replacement)
    ;(println "result: " (clojure.string/replace s (str "docs/" match) replacement))
    (println "result" )
    ;(println index)
    ;(dorun (map-indexed #(println %1 %2) (clojure.string/split result "/")))

    ;(println (clojure.string/split result "/"))


    (if (= replacement "docs/")
      s
      result)))



(re-frame/reg-event-fx
  :toc/navigate-fx
  (fn [{:keys [db]} [_ toc-entry]]
    (re-frisk/add-in-data [:debug :toc :toc/navigate-fx] {:db        db
                                                          :toc-entry toc-entry})
    {:github/file [(:url toc-entry) (:path toc-entry)]}))


(re-frame/reg-event-fx
  :reading/navigate-fx
  [re-frame/debug]
  (fn [cofx]
    (re-frisk/add-in-data [:debug :reading :reading/navigate-fx] {:cofx cofx})
    {}))

(re-frame/reg-event-fx
  :link/navigate-fx
  [re-frame/debug]
  (fn [cofx]
    (re-frisk/add-in-data [:debug :reading :reading/navigate-fx] {:cofx cofx})
    (let [link (last (-> cofx :event))
          root-vec (clojure.string/split (-> cofx :db :root :path) "/")
          child-url (build-child-url (-> cofx :db :root) (clojure.string/join "/" (conj (into [] (drop-last root-vec)) link)))
          path (keyword (clojure.string/join "/" (conj (into [] (drop-last root-vec)) link)))]
      (if (= "#" (first link))
        (do
          (println link)                                    ;; Navigate to anchor on page
          {})
        (do
          ;(println (keyword (clojure.string/join "/" (conj (into [] (drop-last root-vec)) link))))) ;; Navigate to new page
          (println "child-url: " child-url)
          (println "path: " path)

          {:github/file [child-url path]})))))