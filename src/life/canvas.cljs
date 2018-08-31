(ns life.canvas)

(defn fill-rect! [ctx x y width height style]
 (set! (.-fillStyle ctx) style)
 (.fillRect ctx x y width height))

(defn stroke-rect! [ctx x y width height line-style]
 (set! (.-strokeStyle ctx) style)
 (.strokeRect ctx x y width height))

(defn stroke-line! [ctx [x0 y0] [x1 y1] style]
 (set! (.-strokeStyle ctx) style)
 (.moveTo ctx x0 y0)
 (.lineTo ctx x1 y1)
 (.stroke ctx))