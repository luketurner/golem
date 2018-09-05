(ns golem.ui.viewport.tiles
  (:require [reagent.ratom :refer [cursor run! reaction]]
            [golem.ui.viewport.math :refer [base-length calc-origin calc-tile-range base-length tile-in-range? tile->px]]
            [golem.ui.viewport.input :refer [handle-click-event!]]
            [golem.canvas :refer [fill-rect! set-dimensions!]]
            [golem.board :refer [get-current-board]]))

(defn- draw-tile!
  "Draws a single tile onto the canvas. Starts at the lower-left hand corner, and draws up and to the right."
  [ctx viewport tile width]
  (let [[x y] (tile->px viewport tile)]
    (fill-rect! ctx x y width (- width) "black")))

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
    (doseq [tile board] (draw-tile! ctx viewport tile tile-width))))

(defn run-redraw-tiles!
  [!viewport !canvas !board]
  (run!
    (when-let [canvas @!canvas]
      (set-dimensions! canvas @(cursor !viewport [:window])) ; TODO - is there a better option than calling this every time?
      (redraw-tiles! @!viewport @!canvas @!board))))


(defn component
  [!db]
  (let [!viewport (cursor !db [:viewport])
        !board (reaction (get-current-board !db))
        !canvas (cursor !viewport [:canvas :tiles])]
    (run-redraw-tiles! !viewport !canvas !board)
    (fn [!db]
     [:canvas.viewport-tiles {:ref      #(reset! !canvas %)
                              :on-click #(handle-click-event! % !viewport !canvas !db)}])))