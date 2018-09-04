(ns life.ui.header
  (:require [life.ui.game-controls :as game-controls]
            [life.ui.viewport.controls :as viewport-controls]))


(defn component
  [!db]
  [:header
    [:div.game-controls-container [game-controls/component !db]]
    [:p.title
      [:strong "G"] "ame "
      [:strong "O"] "f "
      [:strong "L"] "ife "
      [:strong "EM"] "ulator"]
    [:div.viewport-controls-container [viewport-controls/component !db]]])
