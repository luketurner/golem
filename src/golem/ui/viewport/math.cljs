(ns golem.ui.viewport.math
  (:require [com.rpl.specter :as s]
            [golem.util :refer [floor ceil]]))

; This module mostly does coordinate mapping stuff. Would be nice to get it better abstracted.

(def base-length 10)


(defn tile-in-range? [[[x0 y0] [x1 y1]] [x y]]
  (and (<= x0 x x1) (<= y0 y y1)))

(defn scaled-len
  [viewport]
  (-> viewport (:scale) (* base-length)))

(defn calc-origin
  "Given a viewport settings hash, will calculate the location of the bottom-left corner of the origin (0,0) tile."
  [{[wx wy] :window [ox oy] :offset}]
  [(+ (/ wx 2) ox)
   (+ (/ wy 2) oy)])

(defn px->tile
  "given a pixel x,y coordinate, returns the game tile that the coordinate corresponds to."
  [viewport [x y]]
  (let [len (scaled-len viewport)
        [ox oy] (calc-origin viewport)
        div #(-> % (/ len) (floor))]
    [(div (- x ox)) (div (- oy y))]))

(defn tile->px
  "Converts a tile into an x,y pixel coordinate corresponding to the lower-left corner of the tile's rendered square."
  [viewport [x y]]
  (let [len (scaled-len viewport)
        [ox oy] (calc-origin viewport)]
    [(+ (* x len)    ox)
     (+ (* y len -1) oy)]))


(defn calc-tile-range
  "Given a viewport settings hash, returns the range of \"visible\" tiles to be rendered."
  [viewport [ox oy]]
  (let [{:keys [:scale] :defaults {:scale 1} [wx wy] :window} viewport
        len (* scale base-length)
        viewport-px [[(- ox) (- oy wy)]
                     [(- wx ox) oy]]]
    (s/transform [s/ALL s/ALL] #(floor (/ % len)) viewport-px)))
