(ns life.ui.viewport.tiles
  (:require [reagent.ratom :refer [cursor run! reaction]]
            [life.ui.viewport.math :refer [base-length calc-origin calc-tile-range base-length tile-in-range?]]
            [life.ui.viewport.input :refer [handle-click-event!]]
            [life.canvas :refer [fill-rect! set-dimensions!]]
            [life.board :refer [get-current-board]]))

(defn- draw-tile!
  "Draws a single tile onto the canvas."
  [ctx [tile-x tile-y] width [origin-x origin-y]]
  (let [x (+ origin-x (* width tile-x))
        y (- origin-y (* width tile-y))]
    (fill-rect! ctx x y width width "black")))

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
    (doseq [tile board] (draw-tile! ctx tile tile-width origin))))

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