(ns life.ui.app
 (:require [life.ui.viewport :as viewport]
           [life.ui.header :as header]
           [life.ui.sidebar :as sidebar]))

(defn footer
 [!db]
 [:footer
  [:span "Copyright 2018 Luke Turner"]])

(defn component [!db]
 (let [db @!db
       sidebar? (sidebar/is-open? !db)]
  [:div {:class (if sidebar? "app-container app-container--sidebar" "app-container")}
   [:div.app-header-container [header/component !db]]
   (when sidebar? [:div.app-sidebar-container [sidebar/component !db]])
   ; [controls/component !app-db]
   [:div.app-main-container [viewport/component !db]]
   [:div.app-footer-container [footer !db]]]))