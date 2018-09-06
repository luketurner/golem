(ns golem.db
  (:require [reagent.core :as reagent :refer [atom]]
            [golem.board :refer [default-state] :rename {default-state board-state}]
            [golem.ui.viewport :refer [default-state] :rename {default-state viewport-state}]
            [golem.pattern_manager :refer [default-state] :rename {default-state pattern-manager-state}]
            [cljs.spec.alpha :as s]
            [expound.alpha :refer [expound]]))

;; TODO -- move these specs to a better place
(s/def ::update-loop (s/keys :req-un []))
(s/def ::interval pos-int?)
(s/def ::enabled boolean?)

(s/def ::handlers (s/keys :req-un [::interval]))
(s/def ::interval (s/map-of keyword? (s/nilable int?)))


(s/def ::ui (s/keys :req-un [::sidebar]))
(s/def ::sidebar (s/keys :req-un [::open ::import-ref ::export-ref]))
(s/def ::open boolean?)
(s/def ::import-ref (s/nilable (partial instance? js/HTMLElement)))
(s/def ::export-ref (s/nilable (partial instance? js/HTMLElement)))

(s/def ::db
  (s/keys :req-un [:golem.board.state/board
                   :golem.ui.viewport/viewport
                   :golem.pattern-manager/pattern-manager
                   ::update-loop
                   ::handlers
                   ::ui]))

; default (or initial) state for the app db.
; note: uses def instead of defonce so updates to default-state are live-reloaded.
(def default-state
  {:board           board-state
   :pattern-manager pattern-manager-state
   :update-loop     {:interval 100
                     :enabled  true}
   :handlers        {:interval {}}
   :viewport        viewport-state
   :ui              {:sidebar {:open false
                               :import-ref nil
                               :export-ref nil}}})


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