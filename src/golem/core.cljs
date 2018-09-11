(ns golem.core
  (:require [reagent.core :as reagent]
            [re-frisk.core :as frisk]
            [golem.db :refer [!app-db]]
            [golem.ui.app :as app]
            [golem.board :as board]
            [golem.update_loop :as update-loop]
            [golem.util :refer [debug?]]
            [golem.pattern_manager :as pattern-manager]))

(enable-console-print!)

(defn render-app!
  "Renders the application into the #app element on the screen. Should be called exactly once on app startup.
   React hot-reloading should ensure that components will update even if this is not called on code reloads."
  []
  (reagent/render-component
    [app/component !app-db]
    (. js/document (getElementById "app"))))

(defn init-app!
  "Initializes app state and wires up background handlers -- basically, everything other than rendering.
   Is idempotent and should be called on every code reload, since hot reloading won't magically work for it."
  []
  (update-loop/run-loop! !app-db #(board/step! !app-db))
  (pattern-manager/use-selected-pattern! !app-db))

(defn on-js-reload [] (init-app!))                          ; reset app state+handlers

(when debug?
  (print "debug mode enabled")
  (frisk/enable-frisk! {:x 100 :y 500})
  (frisk/add-data :app-db !app-db))

(render-app!)
(init-app!)
