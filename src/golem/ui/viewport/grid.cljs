(ns golem.ui.viewport.grid
  (:require [reagent.ratom :refer [cursor]]
            [golem.util :refer [run-once!]]
            [golem.ui.viewport.math :refer [base-length calc-origin calc-tile-range]]
            [golem.ui.viewport.canvas :as canvas]
            [golem.ui.viewport.cursors :as cursors]))

(defn canvas-cursor [!viewport] (cursor !viewport [:canvas :grid]))

(defn draw-axes!
  [ctx [win-x win-y] [origin-x origin-y]]
  (canvas/stroke-lines! ctx [[[origin-x 0] [origin-x win-y]]
                             [[0 origin-y] [win-x origin-y]]]
                        "darkblue"))

(defn- draw-grid!
  "Draws the background grid onto the canvas."
  [ctx tile-range tile-width [win-x win-y] [origin-x origin-y]]
  (let [[[x0 y0] [x1 y1]] tile-range
        column-lines (->> (range x0 (inc x1))
                          (map #(+ (* tile-width %) origin-x))
                          (map #(vector [% 0] [% win-y])))
        row-lines (->> (range y0 (inc y1))
                       (map #(- origin-y (* tile-width %)))
                       (map #(vector [0 %] [win-x %])))]
    (canvas/stroke-lines! ctx (into row-lines column-lines) "rgb(210, 210, 210)")
    (draw-axes! ctx [win-x win-y] [origin-x origin-y])))

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
  (run-once! :redraw-grid
    (when-let [canvas @!canvas]
     (canvas/set-dimensions! canvas @(cursors/window !viewport)) ; TODO - is there a better option than calling this every time?
     (redraw-grid! @!viewport @!canvas))))

(defn component
  [!db]
  (let [!viewport (cursors/viewport !db)
        !canvas (canvas-cursor !viewport)]
    (run-redraw-grid! !viewport !canvas)
    (fn [!db]
     [:canvas.viewport-grid {:ref #(reset! !canvas %)}])))
 