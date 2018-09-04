(ns life.ui.viewport.math
  (:require [com.rpl.specter :as s]
            [life.math :refer [floor ceil]]))

; This module mostly does coordinate mapping stuff. Would be nice to get it better abstracted.

(def base-length 10)

(defn calc-origin
  "Given a viewport settings hash, will calculate the location of the top-left corner of the origin (0,0) tile."
  [{[wx wy] :window [ox oy] :offset}]
  [(+ (/ wx 2) ox)
   (+ (/ wy 2) oy)])

(defn calc-tile-range
  "Given a viewport settings hash, returns the range of \"visible\" tiles to be rendered."
  [viewport [ox oy]]
  (let [{:keys [:scale] :defaults {:scale 1} [wx wy] :window} viewport
        len (* scale base-length)
        viewport-px [[(- ox) (- oy)]
                     [(- wx ox) (- wy oy)]]]
    (s/transform [s/ALL s/ALL] #((if (< % 0) floor ceil) (/ % len)) viewport-px)))

(defn tile-in-range? [[[x0 y0] [x1 y1]] [x y]]
  (and (<= x0 x x1) (<= y0 y y1)))