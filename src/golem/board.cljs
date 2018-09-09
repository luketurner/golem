(ns golem.board
  (:require [reagent.ratom :refer [cursor reaction]]
            [cljs.spec.alpha :as s]
            [com.rpl.specter :as st]))

; A board is a HashSet of tiles, e.g. #{[1 2] [4 -1]}
; All of the tiles in the board are considered "alive." (All other tiles are assumed dead.)


; Application state for the board module. With this structure,
; the head of the :history list represents the current board state,
; the 2nd element is the previous state, and so forth.

(s/def ::board (s/coll-of :golem.util/coord :kind set?))
(s/def ::history (s/coll-of ::board :kind seq?))
(s/def ::min-history int?)
(s/def ::max-history int?)
(s/def ::boundary (s/tuple :golem.util/coord :golem.util/coord))
(s/def :golem.board.state/board (s/keys :req-un [::history ::min-history ::max-history ::boundary]))

(def default-state {:history     '()
                    :min-history 75
                    :max-history 100
                    :boundary [[-1000000 -1000000] [1000000 1000000]]})

;; Implementation of game logic (culminating in `step` method)

(defn- neighbors
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

(defn- block
  "Returns the set of all nine points surrounding and including given coord.
   (i.e. the 'block' of the board surrounding it)"
  [tile]
  (conj (neighbors tile) tile))

(defn- enforce-boundary
  [boundary board]
  (let [[[x0 y0] [x1 y1]] boundary]
    (->> board
         (filter (fn [[x y]] (and (<= x0 x x1) (<= y0 y y1))))
         (into #{}))))


(defn- drop-overflow
  [coll min-size max-size]
  (if (< max-size (count coll)) (take min-size coll) coll))

(defn- num-living-neighbors
  "Returns the number of living neighbors on the board for given tile."
  [board tile]
  (->> tile
       (neighbors)
       (reduce #(if (board %2) (inc %1) %1) 0)))

(defn- lives?
  "Returns true if the tile lives on the next step, false if it dies."
  [board tile]
  (let [count (num-living-neighbors board tile)]
    (if (board tile)
      (or (= count 2) (= count 3))
      (= count 3))))

(defn step
  "Increments the board one unit of time."
  [board]
  (let [mlives? (memoize (partial lives? board))]
    (->> board
         (st/select [st/ALL (st/view block) (st/view (partial filter mlives?))])
         (apply concat)
         (set))))

;; Getter functions

(defn get-current-board [!db] (first @(cursor !db [:board :history])))

;; Update functions

(defn undo!
  "Performs an undo operation by popping the most recent element off the history."
  [!db]
  (when (< 1 (count @(cursor !db [:board :history])))
    (swap! !db update-in [:board :history] pop)))

(defn update-board!
  "Generates a new board by calling update-fn on the current board, then pushes the result onto
   the board history. Automatically handles cleaning too-large histories.

   Note that this enforces a boundary on the board. Once tiles reach the boundary, patterns will decohere.
   However, this boundary can be quite large.

   TODO - should cleaning too-large histories be done with a separate do! operation?"
  [!db update-fn]
  (let [min-history @(cursor !db [:board :min-history])
        max-history @(cursor !db [:board :max-history])
        boundary @(cursor !db [:board :boundary])]
    (swap! !db update-in [:board :history]
           (fn [history]
             (-> history
                 (conj (->> history (first) (update-fn) (enforce-boundary boundary)))
                 (drop-overflow min-history max-history))))))

(defn toggle-tile!
  "Mutates board cursor by inserting tile, or removing it if it already exists."
  [!db tile]
  (update-board! !db
                 (fn [old-board]
                   (if (contains? old-board tile)
                     (disj old-board tile)
                     (conj old-board tile)))))

(defn push-board!
  "Pushes a new board to the front of the board history."
  [!db board]
  (update-board! !db #(identity board)))

(defn step! [!app-db] (update-board! !app-db step))

