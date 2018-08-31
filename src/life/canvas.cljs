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