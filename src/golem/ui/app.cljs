(ns golem.ui.app
  (:require [golem.ui.viewport :as viewport]
            [golem.ui.header :as header]
            [golem.ui.sidebar :as sidebar]))

(defn footer
  [!db]
  [:footer
   [:span "Copyright 2018 Luke Turner"]])

(defn component [!db]
  (let [db @!db
        sidebar? (sidebar/is-open? !db)]
    [:div.app-container
      [:div.app-header-container [header/component !db]]
      [:div.app-main-container
        (when sidebar? [:div.app-sidebar-container [sidebar/component !db]])
        [viewport/component !db]]
      [:div.app-footer-container [footer !db]]]))