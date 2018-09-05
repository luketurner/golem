(ns golem.db
  (:require [reagent.core :as reagent :refer [atom]]
            [golem.board :refer [default-state] :rename {default-state board-state}]
            [golem.ui.viewport :refer [default-state] :rename {default-state viewport-state}]
            [golem.pattern-manager :refer [default-state] :rename {default-state pattern-manager-state}]))

; default (or initial) state for the app db.
; note: uses def instead of defonce so updates to default-state are live-reloaded.
(def default-state
  {:board           board-state
   :pattern-manager pattern-manager-state
   :update-loop     {:interval 100
                     :enabled  true}
   :handlers        {:interval {}}
   :viewport        viewport-state
   :ui              {:sidebar {:open false}}})

; App DB holds *all* application state, even ultra-low-level state like interval IDs.
; This allows all state to be introspected by re-frisk, providing maximum visibility.
; However, it does also mean that app state cannot be wantonly serialized and loaded
; without re-running app init code to reset handler IDs and stuff.
(defonce !app-db (atom default-state))

; note -- fairly dangerous, since we lose track of existing interval IDs
(defn reset-state! [] (reset! !app-db default-state))