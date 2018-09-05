(ns golem.ui.viewport.input
  (:require [golem.ui.viewport.math :refer [base-length calc-origin]]
            [golem.math :refer [floor ceil]]
            [golem.board :refer [toggle-tile!]]
            [golem.canvas :refer [get-event-coords]]))

(defn handle-click-event!
  "Contains logic for handling click events anywhere on the viewport canvas.
   Uses the pixel (x,y) coordinates of the click event to determine the board tile
   that the user clicked on, and toggle that tile."
  [click-event !viewport !canvas !db]
  (let [canvas @!canvas
        {:keys [scale] :as viewport} @!viewport
        tile-width (* scale base-length)
        [origin-x origin-y] (calc-origin viewport)
        [clicked-x clicked-y] (get-event-coords click-event canvas)
        tile-x (/ (- clicked-x origin-x) tile-width)
        tile-y (/ (- origin-y clicked-y) tile-width)
        tile-x ((if (neg? tile-x) floor ceil) tile-x)
        tile-y ((if (neg? tile-y) floor ceil) tile-y)]
    (print "detected click:" tile-x tile-y)
    (toggle-tile! !db [tile-x tile-y])))