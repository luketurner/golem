(ns life.ui.viewport.grid
  (:require [reagent.ratom :refer [cursor run!]]
            [life.ui.viewport.math :refer [base-length calc-origin calc-tile-range]]
            [life.canvas :as canvas]))

(defn- draw-grid!
  "Draws the background grid onto the canvas."
  [ctx tile-range tile-width [win-x win-y] [origin-x origin-y]]
  (let [[[x0 y0] [x1 y1]] tile-range
        column-lines (->> (range x0 (inc x1))
                          (map #(+ (* tile-width %) origin-x))
                          (map #(vector [% 0] [% win-y])))
        row-lines (->> (range y0 (inc y1))
                       (map #(+ (* tile-width %) origin-y))
                       (map #(vector [0 %] [win-x %])))]
    (canvas/stroke-lines! ctx (into row-lines column-lines) "rgb(210, 210, 210)")))

(defn redraw-grid!
  "Fully redraws grid onto `canvas` based on `viewport`."
  [viewport canvas]
  (let [{:keys [scale window]} viewport
        ctx (.getContext canvas "2d")
        tile-width (* scale base-length)
        origin (calc-origin viewport)
        tile-range (calc-tile-range viewport origin)]
    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
    (draw-grid! ctx tile-range tile-width window origin)))


(defn run-redraw-grid!
  "Runs redraw-grid! in an observable loop"
  [!viewport !canvas]
  (run!
    (when-let [canvas @!canvas]
     (canvas/set-dimensions! canvas @(cursor !viewport [:window])) ; TODO - is there a better option than calling this every time?
     (redraw-grid! @!viewport @!canvas))))

(defn component
  [!db]
  (let [!viewport (cursor !db [:viewport])
        !canvas (cursor !viewport [:canvas :grid])]
    (run-redraw-grid! !viewport !canvas)
    (fn [!db]
     [:canvas.viewport-grid {:ref #(reset! !canvas %)}])))
 