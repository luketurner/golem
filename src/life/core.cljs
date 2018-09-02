(ns life.core
    (:require [reagent.core :as reagent]
              [reagent.ratom :refer [run! cursor]]
              [com.rpl.specter :as s]
              [re-frisk.core :as frisk]
              [life.db :refer [!app-db]]
              [life.ui.app :as app]
              [life.board :as board]
              [life.update-loop :as update-loop]
              [life.pattern-manager :as pattern-manager]))

(enable-console-print!)

; debug builds include re-frisk and other diagnostic tools missing from production builds.
(def ^boolean debug? ^boolean js/goog.DEBUG)
(when debug? (print "debug mode enabled"))

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

(defn on-js-reload [] (init-app!)) ; reset app state+handlers

(when debug?
  (frisk/enable-frisk! {:x 100 :y 500})
  (frisk/add-data :app-db !app-db))

(render-app!)
(init-app!)
