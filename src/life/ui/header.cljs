(ns life.ui.header
 (:require [life.ui.controls :as controls]))

(defn component
 [!db]
 [:header
  [:div.controls-container [controls/component !db]]
  [:h1.title "Life"]
  [:p.subtitle [:a {:href "https://github.com/luketurner/life"} "github"]]])
