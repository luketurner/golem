(ns life.ui.app
 (:require [life.ui.viewport :as viewport]
           [life.ui.header :as header]))

(defn footer
 [!db]
 [:footer
  [:span "Copyright 2018 Luke Turner"]])

(defn component [!db]
 (let [state @!db] 
  [:div.app-container
   [:div.app-header-container [header/component !db]]
   ; [controls/component !app-db]
   [:div.app-main-container [viewport/component !db]]
   [:div.app-footer-container [footer !db]]]))