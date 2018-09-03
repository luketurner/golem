(ns life.ui.viewport
 (:require [com.rpl.specter :as s]
           [reagent.core :as r]
           [reagent.ratom :refer [cursor run! reaction]]
           [life.math :refer [ceil floor]]
           [life.board :refer [get-current-board update-board!]]
           [life.canvas :refer [fill-rect! stroke-lines! get-event-coords]])) 

(def base-length 10) ; length of a side of a tile, in px

(defonce default-state {:canvas-tiles nil
                        :canvas-grid nil
                        :window [100 100]
                        :offset [0 0]
                        :scale 1.0})

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

(defn draw-grid!
 "Draws the viewport's background grid onto the canvas."
 [ctx tile-range tile-width [win-x win-y] [origin-x origin-y]]
 (let [[[x0 y0] [x1 y1]] tile-range
       column-lines (->> (range x0 (inc x1))
                         (map #(+ (* tile-width %) origin-x))
                         (map #(vector [% 0] [% win-y])))
       row-lines (->> (range y0 (inc y1))
                      (map #(+ (* tile-width %) origin-y))
                      (map #(vector [0 %] [win-x %])))]
  (stroke-lines! ctx (into row-lines column-lines) "rgb(210, 210, 210)")))

(defn draw-tile!
 "Draws a single tile onto the canvas."
 [ctx [tile-x tile-y] width [origin-x origin-y]]
 (let [x (+ origin-x (* width tile-x))
       y (- origin-y (* width tile-y))]
  (fill-rect! ctx x y width width "black")))

(defn redraw-tiles!
 "Fully redraws the viewport canvas based on the current board state and viewport settings."
 [viewport canvas board]
 (let [{:keys [scale window]} viewport
       ctx (.getContext canvas "2d")
       tile-width (* scale base-length)
       origin (calc-origin viewport)
       tile-range (calc-tile-range viewport origin)
       board (filter #(tile-in-range? tile-range %) board)]
  (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
  (doseq [tile board] (draw-tile! ctx tile tile-width origin)))) 

(defn redraw-grid!
 "Fully redraws the viewport canvas based on the current board state and viewport settings."
 [viewport canvas]
 (let [{:keys [scale window]} viewport
       ctx (.getContext canvas "2d")
       tile-width (* scale base-length)
       origin (calc-origin viewport)
       tile-range (calc-tile-range viewport origin)]
  (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
  (draw-grid! ctx tile-range tile-width window origin)))

(defn resize-viewport!
 "Mutates viewport and canvas parameters to set width and height to the 'true' (client) width and height.
  Prevents the canvas from being artificially scaled up or down in the browser.
  Note that resizing the canvas has significant performance cost, so this should only be called when needed."
 [!viewport !canvas]
 (if-let [canvas @!canvas]
  (swap! !viewport assoc :window [(.-clientWidth canvas) (.-clientHeight canvas)])))

(defn run-resize-viewport!
 "Calls resize-viewport! in a run-loop so that it reactively re-runs if any changes are made to the parameters.
  Note: Because resize events are not triggered by JS DOM redraws, we also want to listen for changes to
  the app UI state. This is kludgy, but a workaround for the lack of element-level resize events."
 [!viewport !canvas-tiles !canvas-grid !to-watch]

 ; TODO -- abstract the below run! loops into a better place?
 (run!
  (when-let [canvas-tiles @!canvas-tiles] 
   (set! (.-width canvas-tiles) @(cursor !viewport [:window 0]))
   (set! (.-height canvas-tiles) @(cursor !viewport [:window 1]))))

 (run!
  (when-let [canvas-grid @!canvas-grid]
   (set! (.-width @!canvas-grid) @(cursor !viewport [:window 0]))
   (set! (.-height @!canvas-grid) @(cursor !viewport [:window 1]))))

 (.addEventListener js/window "resize" #(resize-viewport! !viewport !canvas-tiles))
 (run!
  (let [unused @!to-watch] ; deref !to-watch even though we don't care about the value
   (resize-viewport! !viewport !canvas-tiles))))

(defn run-redraw-tiles!
 "Calls redraw-tiles! in a run-loop so that it reactively re-runs if any changes are made to the parameters."
 [!viewport !canvas !board]
 (run!
  (when-let [canvas @!canvas]
   (redraw-tiles! @!viewport canvas @!board))))

(defn run-redraw-grid!
 "Calls redraw-grid! in a run-loop so that it reactively re-runs if any changes are made to the parameters."
 [!viewport !canvas]
 (run!
  (print "trying to redraw grid")
  (when-let [canvas @!canvas]
   (print "redrawing grid")
   (redraw-grid! @!viewport canvas))))

(defn toggle-tile!
 "Mutates board cursor by inserting tile, or removing it if it already exists."
 [!db tile]
 (update-board! !db
  (fn [old-board] 
   (if (contains? old-board tile)
    (disj old-board tile)
    (conj old-board tile)))))
   

(defn handle-click-event!
 "Contains logic for handling click events anywhere on the viewport canvas.
  Uses the pixel (x,y) coordinates of the click event to determine the board tile
  that the user clicked on, and toggle that tile."
 [click-event !viewport !canvas !db]
 (let [canvas @!canvas
       {:keys [scale] :as viewport} @!viewport
       tile-width (* scale base-length)
       [origin-x origin-y] (calc-origin viewport)
       [clicked-x clicked-y] (get-event-coords click-event canvas)
       tile-x (/ (- clicked-x origin-x) tile-width)
       tile-y (/ (- origin-y clicked-y) tile-width)
       tile-x ((if (neg? tile-x) floor ceil) tile-x)
       tile-y ((if (neg? tile-y) floor ceil) tile-y)]
  (print "detected click:" tile-x tile-y)
  (toggle-tile! !db [tile-x tile-y])))

(defn component
 "Reagent component for the viewport, which provides 'view' into the conceptually infinite 'board'.
  The view is defined by an offset (where the offset 0,0 indicates the origin is in the center of the view)
  and by a scale (or 'zoom') factor.
  
  Because the viewport is a Canvas element, we must also run some side-effecting observers to redraw
  the canvas in response to relevant state changes."
 [!app-db]
 (let [!viewport (cursor !app-db [:viewport])
       !board (reaction (get-current-board !app-db))
       !canvas-tiles (cursor !viewport [:canvas-tiles])
       !canvas-grid (cursor !viewport [:canvas-grid])
       !ui (cursor !app-db [:ui])]
  (run-resize-viewport! !viewport !canvas-tiles !canvas-grid !ui)
  (run-redraw-grid! !viewport !canvas-grid)
  (run-redraw-tiles! !viewport !canvas-tiles !board)
  (r/create-class ; TODO -- no longer needs to be a create-class call? change to type 2?
   {:display-name "viewport"
    :reagent-render
    (fn [app-db]
     [:div.viewport-container
      [:canvas.viewport-grid {:ref #(reset! !canvas-grid %)}]
      [:canvas.viewport-tiles
       {:ref #(reset! !canvas-tiles %)
        :on-click #(handle-click-event! % !viewport !canvas-tiles !app-db)}]])})))
    