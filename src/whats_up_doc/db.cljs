(ns whats-up-doc.db)

(def initial-state
  {:font-size              16
   :github-files           {}
   :github-folders         {}
   :toc-panel              {}
   :reading-panel          {}
   :initialization-options {}
   :debug                  {}
   :initialized?           nil
   })


;; :github-files - contains current version of downloaded files. These will eventually be cached in localstorage
;;
;; :github-folders -   contains current version of folders. These are not cached, and the results
;; will be used to determine when individual files need to be refreshed
;;
