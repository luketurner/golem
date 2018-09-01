(ns life.tile)

; A tile is an [x,y] pair representing a coordinate on the board

(defn neighbors
 "Returns the set of all eight adjacent points for the given coord, not including the coord itself."
 [[x y]]
 #{[(inc x) (dec y)]
   [(inc x) y]
   [(inc x) (inc y)]
   [x (dec y)]
   ; [x y] completes the pattern
   [x (inc y)]
   [(dec x) (dec y)]
   [(dec x) y]
   [(dec x) (inc y)]})

(defn block
 "Returns the set of all nine points surrounding and including given coord.
  (i.e. the 'block' of the board surrounding it)"
 [tile]
 (conj (neighbors tile) tile))