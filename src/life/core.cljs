(ns life.core
    (:require [reagent.core :as reagent :refer [atom]]
              [com.rpl.specter :as s]))

(enable-console-print!)

; Board is set of x,y tuples, which are implicitly alive if they exist in the set
(def test-board #{[0 0] [1 0] [2 0] [2 1] [1 2]})

(defonce app-state
 (atom
  {:board test-board
   :viewport [[-100 -100] [100 100]]}))

(defn get-neighbors
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

(defn coord->chunk
 "Returns the set of all nine points surrounding and including given coord.
  (i.e. the 'chunk' of the board surrounding it)"
 [coord]
 (conj (get-neighbors coord) coord))

(defn count-living-neighbors
 "Returns the number of living neighbors for given coordinate."
 [board coord]
 (->> coord
  (get-neighbors)
  (reduce #(if (board %2) (inc %1) %1) 0)))


(defn will-be-alive?
 "Returns true if the coord should be alive on the next step, false if it should be dead."
 [board coord]
 (let [count (count-living-neighbors board coord)]
  (if (board coord)
   (or (= count 2) (= count 3))
   (= count 3))))


(defn next-board
 "Given a board, returns a new board 'stepped ahead' one unit of time."
 [board]
 (->> board
  (map coord->chunk) ; build list of sets of all possibly affected coords
  (reduce into) ; flatten into single set
  (filter #(will-be-alive? board %)) ; remove all coords that should not be alive
  (into #{})))
 
  
(defn header []
 [:h1 "Life"])

(defn tile [alive?] [(if alive? :div.tile.alive :div.tile)])

(defn board-view [board [[x0 y0] [x1 y1]]]
 (into [:div.board-view]
  (for [y (range y0 (inc y1))]
   (into [:div.tile-row]
    (for [x (range x0 (inc x1))] [tile (contains? board [x y])])))))

(defn app []
 (let [state @app-state] 
  [:div#container
   [:nav [header]]
   [:main [board-view (:board state) (:viewport state)]]]))

(reagent/render-component [app]
                          (. js/document (getElementById "app")))

(js/setInterval (fn [] (swap! app-state update :board next-board)) 500)

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

