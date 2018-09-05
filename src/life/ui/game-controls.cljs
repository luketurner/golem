(ns life.ui.game-controls
  (:require [life.update-loop :as update-loop]
            [life.board :as board]
            [life.ui.sidebar :as sidebar]
            [life.pattern-manager :as pattern-manager]))

(defn component [!db]
  [:div.game-controls
    [:div.sidebar [:button {:on-click #(sidebar/toggle! !db)} (if (sidebar/is-open? !db) "hide" "show") " patterns"]]
    [:div.cmds-label "Controls: "]
    [:div.cmds
      [:button {:on-click #(board/undo! !db)} "undo"]
      [:button {:on-click #(update-loop/toggle! !db)} (if (update-loop/is-enabled? !db) "pause" "play")]
      [:button {:on-click #(pattern-manager/use-selected-pattern! !db)} "reset"]]
    [:div.rates-label "Update Rate: "]
    [:div.rates
      [:button {:on-click #(update-loop/inc-rate! !db 1000)} "--"]
      [:button {:on-click #(update-loop/inc-rate! !db 100)} "-"]
      [:span (str (update-loop/get-rate !db) " ms")]
      [:button {:on-click #(update-loop/inc-rate! !db -100)} "+"]
      [:button {:on-click #(update-loop/inc-rate! !db -1000)} "++"]]])