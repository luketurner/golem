(ns golem.ui.viewport
  (:require [com.rpl.specter :as s]
            [reagent.core :as r]
            [reagent.ratom :refer [cursor run! reaction]]
            [golem.math :refer [ceil floor]]
            [golem.board :refer [get-current-board update-board!]]
            [golem.canvas :refer [fill-rect! stroke-lines! get-event-coords]]
            [golem.ui.viewport.grid :as grid]
            [golem.ui.sidebar :as sidebar]
            [golem.ui.viewport.tiles :as tiles]))

(def default-state {:canvas-tiles nil
                    :canvas-grid  nil
                    :window       [100 100] ; width of the window (in px)
                    :offset       [0 0]     ; Distance between the board origin and the center of the screen (in px)
                    :scale        1.0})     ; factor to scale when converting from tiles->px


(defn resize-viewport!
  "Mutates :window to match the actual rendered width/height in the client browsers.
   Effectively allows us to size the element with CSS and scale our rendering logic appropriately."
  [!viewport !el]
  (when-let [el @!el]
    (println "resize-viewport!")

    ; TODO -- if this doesn't mutate !viewport (because the values are unchanged) does it still trigger observers?
    (swap! !viewport assoc :window [(.-clientWidth el) (.-clientHeight el)])))

(defn run-resize-viewport!
  "Calls resize-viewport! in a run-loop so that it re-runs if any changes are made to the parameters.
   Note -- for the time being, since we have no good way to detect resize events on a specific element,
   we have to 'observe' certain properties in the UI state as a proxy."
  [!viewport !el]
  (.addEventListener js/window "resize" #(resize-viewport! !viewport !el))
  (run!
    (resize-viewport! !viewport !el)))

(defn component
  "Reagent component for the viewport, which provides 'view' into the conceptually infinite 'board'.
   The view is defined by an offset (where the offset 0,0 indicates the origin is in the center of the view)
   and by a scale (or 'zoom') factor."
  [!db]
  (let [!viewport (cursor !db [:viewport])
        !board (reaction (get-current-board !db))
        !canvas-container (cursor !viewport [:canvas :container])
        !ui (cursor !db [:ui])]

    (run-resize-viewport! !viewport !canvas-container)

    (fn [!db]
     ; use :ref to track the container element so we can inspect the client rendered x,y size.
     [:div.viewport-container {:ref #(reset! !canvas-container %)}
      [grid/component !db]
      [tiles/component !db]])))