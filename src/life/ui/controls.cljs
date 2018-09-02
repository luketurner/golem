(ns life.ui.controls
  (:require [life.update-loop :as update-loop]))

(defn component [!db]
 [:div.controls
  [:div.cmds-label "Controls: "]
  [:div.cmds
   
   [:button {:on-click #(update-loop/toggle! !db)} (if (update-loop/is-enabled? !db) "pause" "play")]]
  [:div.rates-label "Update Rate: "]
  [:div.rates
   [:button {:on-click #(update-loop/inc-rate! !db 1000)} "--"]
   [:button {:on-click #(update-loop/inc-rate! !db 100)} "-"]
   [:span (str (update-loop/get-rate !db) " ms")]
   [:button {:on-click #(update-loop/inc-rate! !db -100)} "+"]
   [:button {:on-click #(update-loop/inc-rate! !db -1000)} "++"]]])