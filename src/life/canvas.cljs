(ns life.canvas)

(defn fill-rect! [ctx x y width height style]
 (set! (.-fillStyle ctx) style)
 (.fillRect ctx x y width height))

(defn stroke-lines! [ctx coord-pairs style]
 (set! (.-strokeStyle ctx) style)
 (doseq [[[x0 y0] [x1 y1]] coord-pairs]
  (.moveTo ctx x0 y0)
  (.lineTo ctx x1 y1))
 (.stroke ctx))

(defn get-event-coords
 "Given a JS MouseEvent and a Canvas element, calculates the x,y position of the MouseEvent within the
  Canvas. e.g. if you clicked in the top left corner of the canvas, it would return [0, 0]."
 [event canvas]
 (let [canvas-rect (.getBoundingClientRect canvas)
       canvas-x (.-left canvas-rect)
       canvas-y (.-top canvas-rect)]  
  [(- (.-clientX event) canvas-x)
   (- (.-clientY event) canvas-y)]))