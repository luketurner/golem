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
      [:button.left {:on-click #(shift-offset! !db [1 0])} "<"]
      [:button.up {:on-click #(shift-offset! !db [0 1])} "^"]
      [:button.down {:on-click #(shift-offset! !db [0 -1])} "v"]
      [:button.right {:on-click #(shift-offset! !db [-1 0])} ">"]
      [:button.scale-down {:on-click #(rescale! !db 0.9)} "-"]
      [:button.scale-up {:on-click #(rescale! !db 1.1)} "+"]]))