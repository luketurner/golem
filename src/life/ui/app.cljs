(ns life.ui.app
 (:require [life.ui.viewport :as viewport]
           [life.ui.controls :as controls]))

(defn component [!app-db]
 (let [state @!app-db] 
  [:div#container
   [controls/component !app-db]
   [:main
    [viewport/component !app-db]]]))