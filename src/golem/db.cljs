(ns golem.db
  (:require [reagent.core :as reagent :refer [atom]]
            [golem.board :refer [default-state] :rename {default-state board-state}]
            [golem.pattern_manager :refer [default-state] :rename {default-state pattern-manager-state}]
            [golem.update_loop :refer [default-state] :rename {default-state update-loop-state}]
            [golem.handlers :refer [default-state] :rename {default-state handlers-state}]
            [golem.ui.app :refer [default-state] :rename {default-state ui-state}]
            [cljs.spec.alpha :as s]
            [expound.alpha :refer [expound]]))


(s/def ::db
  (s/keys :req-un [:golem.board.state/board
                   :golem.pattern_manager/pattern-manager
                   :golem.update_loop/update-loop
                   :golem.handlers/handlers
                   :golem.ui.app/ui]))

; default (or initial) state for the app db.
; note: uses def instead of defonce so updates to default-state are live-reloaded.
(def default-state
  {:board           board-state
   :pattern-manager pattern-manager-state
   :update-loop     update-loop-state
   :handlers        handlers-state
   :ui              ui-state})


(defn valid-state?
  [state]
  (let [valid? (s/valid? ::db state)]
    (when-not valid?
      (println "Rejecting invalid application state:")
      (expound ::db state)) ; TODO -- WOW, can this get slow.
    valid?))

; App DB holds *all* application state, even ultra-low-level state like interval IDs.
; This allows all state to be introspected by re-frisk, providing maximum visibility.
; However, it does also mean that app state cannot be wantonly serialized and loaded
; without re-running app init code to reset handler IDs and stuff.
(defonce !app-db (atom default-state :validator valid-state?))

; note -- fairly dangerous, since we lose track of existing interval IDs
(defn reset-state! [] (reset! !app-db default-state))