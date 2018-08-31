(ns life.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.ratom :refer [run! cursor]]
              [com.rpl.specter :as s]
              [life.viewport :refer [viewport]]
              [re-frisk.core :as frisk]))

(def ^boolean debug? ^boolean js/goog.DEBUG)
(when debug? (print "debug mode enabled"))

(enable-console-print!)

; Board is set of x,y tuples, which are implicitly alive if they exist in the set
(def test-board #{[0 0] [1 0] [2 0] [2 1] [1 2]})

(def default-size 10) ;px

(defonce !app-db
 (atom
  {:board test-board
   :updater {:interval 500}
   :handlers {:interval {}}
   :viewport {:canvas nil
              :window [100 100]
              :offset [0 0]
              :scale 1.0}}))



(defn set-interval! [key interval function]
 (swap! !app-db update-in [:handlers :interval key]
  (fn [old-interval-id]
   (when (number? old-interval-id)
    (js/clearInterval old-interval-id))
   (js/setInterval function interval))))

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
 [:nav#header [:h1 "Life"]])

(defn app []
 (let [state @!app-db] 
  [:div#container
   [header]
   [:main [viewport !app-db]]]))

(defn render []
 (reagent/render-component
  [app]
  (. js/document (getElementById "app"))))

(defn step-board! []
 (swap! !app-db update :board next-board))

(defn run-update-loop! []
 (let [!interval (cursor !app-db [:updater :interval])]
  (run! (set-interval! :update-loop @!interval step-board!))))

(when debug?
  (frisk/enable-frisk! {:x 100 :y 500})
  (frisk/add-data :app-db !app-db))

(render)
(run-update-loop!)

(defn on-js-reload []
 (run-update-loop!) ; todo -- is this necessary?
 (swap! !app-db assoc :board test-board)) ; reset board state
 
  ;; optionally touch your !app-db to force rerendering depending on
  ;; your application
  ;; (swap! !app-db update-in [:__figwheel_counter] inc)

