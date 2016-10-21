(ns whats-up-doc.db)

(def initial-state
  {:branch                 nil
   :font-size              16
   :files                  {}
   :github-files           {}                               ; cached to localstorage.
   :github-folders         {}                               ; not cached, used to determine when to refresh cached files
   :initialization-options {}
   :initialized?           nil
   :reading-panel          {}
   :toc-panel              {}

   })
