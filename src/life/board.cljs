(ns life.board
 (:require [life.tile :as tile]))

; A board is a set of tiles.

(defn num-living-neighbors
 "Returns the number of living neighbors on the board for given tile."
 [board tile]
 (->> tile
  (tile/neighbors)
  (reduce #(if (board %2) (inc %1) %1) 0)))

(defn lives?
 "Returns true if the tile lives on the next step, false if it dies."
 [board tile]
 (let [count (num-living-neighbors board tile)]
  (if (board tile)
   (or (= count 2) (= count 3))
   (= count 3))))


(defn step
 "Increments the board one unit of time."
 [board]
 (->> board
  (map tile/block) ; build list of sets of all possibly affected coords
  (reduce into) ; flatten into single set
  (filter #(lives? board %)) ; remove all coords that should not be alive
  (into #{})))

(defn step! [!app-db]
 (swap! !app-db update :board step))