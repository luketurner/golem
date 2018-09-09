(ns golem.ui.viewport.input
  (:require [golem.ui.viewport.math :refer [base-length calc-origin px->tile]]
            [golem.ui.viewport.canvas :refer [get-event-coords]]
            [golem.math :refer [floor ceil]]
            [golem.board :refer [toggle-tile!]]))


(defn handle-click-event!
  "Contains logic for handling click events anywhere on the viewport canvas.
   Uses the pixel (x,y) coordinates of the click event to determine the board tile
   that the user clicked on, and toggle that tile."
  [click-event !viewport !canvas !db]
  (let [canvas @!canvas
        viewport @!viewport
        [tile-x tile-y] (px->tile viewport (get-event-coords click-event canvas))]
    (print "detected click:" tile-x tile-y)
    (toggle-tile! !db [tile-x tile-y])))