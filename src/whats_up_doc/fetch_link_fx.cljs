(ns whats-up-doc.fetch-link-fx
  (:require [re-frame.core :as re-frame]
            ))




(re-frame/reg-event-fx
  :external-link-fx
  ;; Is this required? What's the point?
  {})



(re-frame/reg-fx
  :internal-link-fx
  ;; When an internal link is clicked, do the following (or some approximation)
  ;; 1) Check to see if file exists in the db
  ;; 2) If results exist in DB, then check to see if the SHA is the same (should be pre-loaded from other actions)
  ;; 3) If SHA doesn't match or doesn't exist, then re-fetch the document
  ;; 4) Render the internal-link
  ;; TODO - Decide when and how to load folder contents

  (fn [cofx]
    (println ":fetch-link-fx cofx" cofx)
    {:fetch-link "Link fetched?"}
    ))


(re-frame/reg-fx
  :root-file-fx

  (fn root [cofx]
    (println ":root-file-fx " cofx)
    {}))