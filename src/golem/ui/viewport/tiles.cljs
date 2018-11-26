(ns golem.ui.viewport.tiles
  (:require-macros [golem.util :refer [run-once!]])
  (:require [reagent.ratom :refer [cursor reaction]]
            [golem.ui.viewport.math :refer [base-length calc-origin calc-tile-range base-length tile-in-range? tile->px]]
            [golem.ui.viewport.input :refer [handle-click-event!]]
            [golem.ui.viewport.cursors :as cursors]
            [golem.ui.viewport.canvas :refer [fill-path! set-dimensions!]]
            [golem.board :refer [current-board]]))

(defn canvas-cursor [!viewport] (cursor !viewport [:canvas :tile]))

(defn- path-tile!
  "Draws a single tile onto the canvas. Starts at the lower-left hand corner, and draws up and to the right."
  [ctx viewport tile width]
  (let [[x y] (tile->px viewport tile)]
    (.rect ctx x y width (- width) "black")))

(defn redraw-tiles!
  "Redraws all of the tiles based on the current board state and viewport settings."
  [viewport canvas board]
  (let [{:keys [scale window]} viewport
        ctx (.getContext canvas "2d")
        tile-width (* scale base-length)
        origin (calc-origin viewport)
        tile-range (calc-tile-range viewport origin)
        board (filter #(tile-in-range? tile-range %) board)]
    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
    (.beginPath ctx)
    (doseq [tile board] (path-tile! ctx viewport tile tile-width))
    (fill-path! ctx "black")))

(defn run-redraw-tiles!
  [!viewport !canvas !board]
  (run-once! :redraw-tiles
    (when-let [canvas @!canvas]
      (set-dimensions! canvas @(cursors/window !viewport)) ; TODO - is there a better option than calling this every time?
      (redraw-tiles! @!viewport @!canvas @!board))))


(defn component
  [!db]
  (let [!viewport (cursors/viewport !db)
        !board (current-board !db)
        !canvas (canvas-cursor !viewport)]
    (run-redraw-tiles! !viewport !canvas !board)
    (fn [!db]
     [:canvas.viewport-tiles {:ref      #(reset! !canvas %)
                              :on-click #(handle-click-event! % !viewport !canvas !db)}])))