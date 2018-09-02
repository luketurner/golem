(ns life.pattern-manager
 (:require [reagent.ratom :refer [cursor]]
           [life.pattern :as pattern]))

; Module for maintaining a list of saved patterns that can be loaded later.
; Used for three things:
; 1. Maintaining list of interesting patterns for users to choose from.
; 2. Allowing users to import/export patterns in RLE format
; 3. Allowing user to save current board as a pattern (simiar to "bookmarking" it)

(def default-patterns
 [(pattern/rle->pattern "#N Glider
                         #O http://www.conwaylife.com/wiki/Glider
                         x = 3, y = 3
                         bo$2bo$3o!")
  (pattern/rle->pattern "#N Gosper glider gun
                         #O http://www.conwaylife.com/wiki/Gosper_glider_gun
                         x = 36, y = 9, rule = B3/S23
                         24bo11b$22bobo11b$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o14b$
                         2o8bo3bob2o4bobo11b$10bo5bo7bo11b$11bo3bo20b$12b2o!")])

(def default-state {:selected (first default-patterns)
                    :saved default-patterns})

(defn saved-patterns [!db] @(cursor !db [:pattern-manager :saved]))
(defn selected-pattern [!db] @(cursor !db [:pattern-manager :selected]))

(defn use-selected-pattern!
 "Uses the currently-selected pattern, displaying it on the screen."
 [!db]
 (pattern/use-pattern! !db (selected-pattern !db)))