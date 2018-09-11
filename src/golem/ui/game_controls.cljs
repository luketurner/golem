(ns golem.ui.game_controls
  (:require [golem.update_loop :as update-loop]
            [golem.board :as board]
            [golem.ui.sidebar :as sidebar]
            [golem.pattern_manager :as pattern-manager]))

(defn component [!db]
  [:div.game-controls
    [:button.sidebar-toggle {:on-click #(sidebar/toggle! !db)} (if @(sidebar/is-open? !db) "hide" "show") " patterns"]
    [:button.undo {:disabled true :title "Temporarily disabled for performance reasons"} "undo"]
    [:button.pause {:on-click #(update-loop/toggle! !db)} (if @(update-loop/is-enabled? !db) "pause" "play")]
    [:button.reset {:on-click #(pattern-manager/use-selected-pattern! !db)} "reset"]
    [:button.mm {:on-click #(update-loop/inc-fps! !db -10)} "--"]
    [:button.m {:on-click #(update-loop/inc-fps! !db -1)} "-"]
    [:div.rate (str @(update-loop/fps !db) " fps")]
    [:button.p {:on-click #(update-loop/inc-fps! !db 1)} "+"]
    [:button.pp {:on-click #(update-loop/inc-fps! !db 10)} "++"]])