(ns golem.canvas)

(defn fill-rect! [ctx x y width height style]
  (set! (.-fillStyle ctx) style)
  (.fillRect ctx x y width height))

(defn stroke-lines! [ctx coord-pairs style]
  (set! (.-strokeStyle ctx) style)
  (.beginPath ctx)
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

(defn set-dimensions!
  [canvas [w h]]
  (let [cw (.-width canvas)
        ch (.-height canvas)]
    (when-not (= w cw) (set! (.-width canvas) w))
    (when-not (= h ch) (set! (.-height canvas) h))))