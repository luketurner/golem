(ns golem.ui.app
  (:require [golem.ui.viewport :as viewport]
            [golem.ui.header :as header]
            [golem.ui.sidebar :as sidebar]))

(defn footer
  [!db]
  [:footer
   [:div "Copyright 2018 Luke Turner"]
   [:div [:a {:href "https://github.com/luketurner/golem"} "Github"]]])

(defn component [!db]
  (let [db @!db
        sidebar? (sidebar/is-open? !db)]
    [:div.app-container
      [:div.app-header-container [header/component !db]]
      [:div.app-main-container
        (when sidebar? [:div.app-sidebar-container [sidebar/component !db]])
        [viewport/component !db]]
      [:div.app-footer-container [footer !db]]]))