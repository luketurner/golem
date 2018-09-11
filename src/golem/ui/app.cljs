(ns golem.ui.app
  (:require [golem.ui.viewport :as viewport]
            [golem.ui.header :as header]
            [golem.ui.sidebar :as sidebar]
            [cljs.spec.alpha :as s]))

(s/def ::ui (s/keys :req-un [:golem.ui.sidebar/sidebar
                             :golem.ui.viewport/viewport]))

(def default-state {:sidebar sidebar/default-state
                    :viewport viewport/default-state})

(defn footer
  [!db]
  [:footer
   [:div "Copyright 2018 Luke Turner"]
   [:div [:a {:href "https://github.com/luketurner/golem"} "Github"]]])

(defn component [!db]
  (let [sidebar? @(sidebar/is-open? !db)]
    [:div.app-container
      [:div.app-header-container [header/component !db]]
      [:div.app-main-container
        (when sidebar? [:div.app-sidebar-container [sidebar/component !db]])
        [viewport/component !db]]
      [:div.app-footer-container [footer !db]]]))