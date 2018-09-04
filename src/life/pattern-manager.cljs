(ns life.pattern-manager
  (:require [reagent.ratom :refer [cursor]]
            [life.pattern :as pattern]))

; Module for maintaining a list of saved patterns that can be loaded later.
; Used for three things:
; 1. Maintaining list of interesting patterns for users to choose from.
; 2. Allowing users to import/export patterns in RLE format
; 3. Allowing user to save current board as a pattern (simiar to "bookmarking" it)

(def default-patterns
  {1 (pattern/rle->pattern "#N Glider
                            #O http://www.conwaylife.com/wiki/Glider
                            x = 3, y = 3
                            bo$2bo$3o!")
   2 (pattern/rle->pattern "#N Gosper glider gun
                            #O http://www.conwaylife.com/wiki/Gosper_glider_gun
                            x = 36, y = 9, rule = B3/S23
                            24bo11b$22bobo11b$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o14b$
                            2o8bo3bob2o4bobo11b$10bo5bo7bo11b$11bo3bo20b$12b2o!")})

(def default-state {:selected  (first (keys default-patterns))
                    :saved     default-patterns
                    :latest-id (apply max (keys default-patterns))})

(defn saved-patterns [!db] @(cursor !db [:pattern-manager :saved]))
(defn selected-pattern-id [!db] @(cursor !db [:pattern-manager :selected]))
(defn selected-pattern [!db] (get (saved-patterns !db) (selected-pattern-id !db)))
(defn latest-id [!db] @(cursor !db [:pattern-manager :latest-id]))
(defn pop-id! [!db] (swap! (cursor !db [:pattern-manager :latest-id]) inc))

(defn import-pattern!
  "Imports `pattern` into the pattern manager in `!db`. Returns the ID of the inserted pattern."
  [!db pattern]
  (let [id (pop-id! !db)]
    (swap! !db assoc-in [:pattern-manager :saved id] pattern)
    id))

(defn select-pattern!
  "Chooses `pattern` as the active selection in the pattern manager in `!db`."
  [!db id]
  (swap! !db assoc-in [:pattern-manager :selected] id))

(defn use-selected-pattern!
  "Uses the currently-selected pattern, displaying it on the screen."
  [!db]
  (pattern/use-pattern! !db (selected-pattern !db)))

(defn select-and-use-pattern!
  "Chooses `pattern` as the active selection in the pattern manager in `!db` and displays it on the screen."
  [!db id]
  (select-pattern! !db id)
  (use-selected-pattern! !db))

(defn import-select-and-use-pattern!
  "Imports `pattern`, selects it, and displays it on the screen."
  [!db pattern]
  (let [id (import-pattern! !db pattern)]
    (select-and-use-pattern! !db id)))