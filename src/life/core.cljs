(ns life.core
    (:require [reagent.core :as reagent]
              [reagent.ratom :refer [run! cursor]]
              [com.rpl.specter :as s]
              [re-frisk.core :as frisk]
              [life.db :refer [!app-db]]
              [life.ui.app :as app]
              [life.board :as board]
              [life.update-loop :as update-loop]))

(def ^boolean debug? ^boolean js/goog.DEBUG)
(when debug? (print "debug mode enabled"))

(enable-console-print!)

; Board is set of x,y tuples, which are implicitly alive if they exist in the set
(def test-board #{[0 0] [1 0] [2 0] [2 1] [1 2]})

(def default-size 10) ;px

(defn render-app []
 (reagent/render-component
  [app/component !app-db]
  (. js/document (getElementById "app"))))

(defn init-app!
 []
 (update-loop/run-loop! !app-db #(board/step! !app-db))
 (swap! !app-db assoc :board test-board))


(when debug?
  (frisk/enable-frisk! {:x 100 :y 500})
  (frisk/add-data :app-db !app-db))

(render-app)
(init-app!)

(defn on-js-reload [] (init-app!)) ; reset board state
