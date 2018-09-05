(ns life.ui.viewport.controls
  (:require [reagent.ratom :refer [cursor]]
            [life.ui.viewport.math :refer [base-length]]
            [com.rpl.specter :as s]))

(defn shift-offset!
  [!db offset]
  (swap! !db update-in [:viewport :offset] #(map + % (map (partial * base-length) offset)))) ; todo -abstract this better
(defn rescale! [!db mult] (swap! (cursor !db [:viewport :scale]) * mult))

(defn component [!db]
  (let [!scale (cursor !db [:viewport :scale])]
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