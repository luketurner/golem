(ns life.ui.viewport.controls
  (:require [life.ui.viewport :as viewport]
            [reagent.ratom :refer [cursor]]))

(defn rescale! [!db mult] (swap! (cursor !db [:viewport :scale]) * mult))

(defn component [!db]
  (let [!scale (cursor !db [:viewport :scale])]
    [:div.viewport-controls
      [:div.movers-label "Center: "]
      [:div.movers
        [:span "X,Y"]
        [:button {:on-click #()} "<"]
        [:button {:on-click #()} "^"]
        [:button {:on-click #()} "v"]
        [:button {:on-click #()} ">"]]
      [:div.scalers-label "Zoom: "]
      [:div.scalers
        [:button {:on-click #(rescale! !db 0.9)} "-"]
        [:span @!scale]
        [:button {:on-click #(rescale! !db 1.1)} "+"]]]))