(ns life.viewport
 (:require [com.rpl.specter :as s]
           [reagent.core :as r]
           [reagent.ratom :refer [cursor run!]]
           [life.math :refer [ceil floor]]  
           [life.canvas :refer [fill-rect! stroke-rect! stroke-line!]]))

(def base-length 10) ; length of a side of a tile, in px

(defn calc-origin
 "Given a viewport, will calculate the location of the top-left corner of the origin (0,0) tile."
 [{[wx wy] :window [ox oy] :offset}]
 [(+ (/ wx 2) ox) 
  (+ (/ wy 2) oy)])

(defn calc-tile-range
 "Given a viewport, returns the range of \"visible\" tiles to be rendered."
 [viewport]
 (let [{:keys [:scale] :defaults {:scale 1} [wx wy] :window} viewport
       len (* scale base-length)
       [ox oy] (calc-origin viewport)
       viewport-px [[(- ox) (- oy)]
                    [(- wx ox) (- wy oy)]]]
   (s/transform [s/ALL s/ALL] #((if (< % 0) floor ceil) (/ % len)) viewport-px)))

(defn draw-tile! [ctx {:keys [scale] :as viewport} [cx cy] alive?]
 (let [len (* scale base-length)
       [ox oy] (calc-origin viewport)
       x (+ ox (* len cx))
       y (- oy (* len cy))]
  (if alive?
   (fill-rect! ctx x y len len "black")
   (stroke-rect! ctx x y len len "grey"))))

(defn tile-in-range? [[[x0 y0] [x1 y1]] [x y]]
 (and (<= x0 x x1) (<= y0 y y1)))

(defn draw-viewport! [canvas viewport board]
 (let [{:keys [scale]} viewport
       ctx (.getContext canvas "2d")
       length (* scale base-length)
       tile-range (calc-tile-range viewport)
       board (filter #(tile-in-range? tile-range %) board)]
  (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
  (doseq [tile board] (draw-tile! ctx viewport tile true)))) 

(defn resize-window! [!viewport canvas]
 (let [width (.-clientWidth canvas)
       height (.-clientHeight canvas)]
  (swap! !viewport assoc :window [width height])
  (set! (.-width canvas) width)
  (set! (.-height canvas) height)))
      
(defn viewport
 "Reagent component for the viewport. "
 [!app-db]
 (let [!viewport (cursor !app-db [:viewport])
       !board (cursor !app-db [:board])
       !canvas (cursor !viewport [:canvas])]
  (run! ; Resize canvas to dynamically fit window (instead of scaling)
   (when-let [canvas @!canvas]
    (.addEventListener js/window "resize" #(resize-window! !viewport canvas))
    (resize-window! !viewport canvas)))
  (run!
   (when-let [canvas @!canvas]
    (draw-viewport! canvas @!viewport @!board))) ; TODO -- set dirty flag and handle it in requestAnimationFrame?
  (r/create-class ; TODO -- no longer needs to be a create-class call? change to type 2?
   {:display-name "viewport"
    :reagent-render
    (fn [app-db]
     [:canvas#viewport {:ref #(reset! !canvas %)}])}))) 
    