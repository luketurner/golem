(ns life.ui.header
 (:require [life.ui.game-controls :as game-controls]
           [life.ui.viewport.controls :as viewport-controls]))
 

(defn component
 [!db]
 [:header
  [:div.game-controls-container [game-controls/component !db]]
  [:h1.title "Life"]
  [:p.subtitle [:a {:href "https://github.com/luketurner/life"} "github"]]
  [:div.viewport-controls-container [viewport-controls/component !db]]])
