(ns golem.ui.viewport.controls
  (:require [reagent.ratom :refer [cursor]]
            [golem.ui.viewport.math :refer [base-length]]
            [com.rpl.specter :as s]
            [golem.ui.viewport.cursors :as cursors]))

(defn shift-offset!
  [!db offset]
  (swap! (cursors/offset (cursors/viewport !db)) #(vec (map + % (map (partial * base-length) offset))))) ; todo -abstract this better
(defn rescale! [!db mult] (swap! (cursors/scale (cursors/viewport !db)) * mult))

(defn component [!db]
  (let [!scale (cursor !db [:ui :viewport :scale])]
    [:div.viewport-controls
      [:div.movers-label "Center: "]
      [:div.movers
        [:span "X,Y"]
        [:button {:on-click #(shift-offset! !db [1 0])} "<"]
        [:button {:on-click #(shift-offset! !db [0 1])} "^"]
        [:button {:on-click #(shift-offset! !db [0 -1])} "v"]
        [:button {:on-click #(shift-offset! !db [-1 0])} ">"]]
      [:div.scalers-label "Zoom: "]
      [:div.scalers
        [:button {:on-click #(rescale! !db 0.9)} "-"]
        [:span @!scale]
        [:button {:on-click #(rescale! !db 1.1)} "+"]]]))