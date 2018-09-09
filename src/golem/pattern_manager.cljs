(ns golem.pattern_manager
  (:require [reagent.ratom :refer [cursor]]
            [golem.rle :as rle]
            [cljs.spec.alpha :as s]
            [golem.board :as board]))

; Module for maintaining a list of saved patterns that can be loaded later.
; Used for three things:
; 1. Maintaining list of interesting patterns for users to choose from.
; 2. Allowing users to import/export patterns in RLE format
; 3. Allowing user to save current board as a pattern (simiar to "bookmarking" it)

(s/def ::pattern (s/keys :req-un [::name
                                  ::dimensions
                                  :golem.pattern.rle/pattern
                                  :golem.pattern.rle/pattern-ast
                                  :golem.board/board]
                         :opt-un [::origin
                                  ::offset
                                  ::comments]))

(s/def ::selected int?)
(s/def ::latest-id int?)
(s/def ::patterns (s/map-of int? ::pattern))
(s/def ::pattern-manager (s/keys :req-un [::selected ::patterns ::latest-id]))

; Initial list of patterns mostly copied from "Important Patterns" on http://www.conwaylife.com
; original authors and comments are preserved.
(def default-patterns
  (->> ["#N Acorn
         #O Charles Corderman
         #C A methuselah with lifespan 5206.
         #C www.conwaylife.com/wiki/index.php?title=Acorn
         x = 7, y = 3, rule = B3/S23
         bo5b$3bo3b$2o2b3o!"

        "#N Gosper glider gun
         #O Bill Gosper
         #C A true period 30 glider gun.
         #C The first known gun and the first known finite pattern with unbounded growth.
         #C www.conwaylife.com/wiki/index.php?title=Gosper_glider_gun
         x = 36, y = 9, rule = B3/S23
         24bo11b$22bobo11b$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o14b$2o8b
         o3bob2o4bobo11b$10bo5bo7bo11b$11bo3bo20b$12b2o!"

        "#N Gosper glider gun (glider destruction)
         #O Dean Hickerson
         #C Complete destruction of Gosper glider gun with two gliders
         #C Glider destruction of the Gosper glider gun.
         #C http://www.conwaylife.com/wiki/Gosper_glider_gun
         #C http://www.conwaylife.com/patterns/gosperglidergungliderdestruction.rle
         x = 47, y = 26, rule = B3/S23
         bo$2bo$3o6$15bo$15b4o$16b4o10b2o$5b2o9bo2bo9bobo$5b2o9b4o8b3o8b2o$15b
         4o8b3o9b2o$15bo12b3o$29bobo$30b2o7$45b2o$44b2o$46bo!"

        "#N Glider
         #O Richard K. Guy
         #C The smallest, most common, and first discovered spaceship. Diagonal, has period 4 and speed c/4.
         #C www.conwaylife.com/wiki/index.php?title=Glider
         x = 3, y = 3, rule = B3/S23
         bob$2bo$3o!"

        "#N Glider_synth
         #O Mark D. Niemiec
         #C Glider synthesis of a glider.
         #C http://home.interserv.com/~mniemiec/objname.htm#G)})
         x = 110, y = 43, rule = 23/3
         87b5o18b$87bo4bo17b$87bo22b$88bo3bo17b$90bo19b$87bo22b$86b3o21b$5bo80b
         ob2o20b$6bo2bo77b3o20b$4b3o2b2o34bobo39b3o20b$8bobo16b3o16b2o19b3o17b
         2o18b3o$27bo18bo20bo39bo2b$28bo39bo39bob$45b2o63b$44b2o64b$46bo63b15$
         46b2o62b$46bobo61b$47bo62b$42b3o65b$b2o41bo43b2o20b$obo40bo38b2o3bobo
         20b$2bo2b2o76b2obobo21b$5bo76bo4bo22b$6b3o101b$8bo101b$27b3o37b3o37b3o
         $27bo39bo39bo2b$28bo39bo39bo!"

        "#N switchengine_synth
         #O Luka Okanishi, 12 March 2017
         #C 3-glider synthesis of an unstabilized switch engine
         #C www.conwaylife.com/wiki/index.php?title=Switch_engine
         #C http://www.conwaylife.com/patterns/switchengine_synth.rle
         x = 14, y = 8, rule = B3/S23
         obo$b2o$bo6bo$7bo$7b3o$11b2o$11bobo$11bo!"]
       (map #(vector %1 (rle/parse-rle %2)) (range 1))
       (into {})))

(def default-state {:patterns  default-patterns
                    :selected  (first (keys default-patterns))
                    :latest-id (apply max (keys default-patterns))})


;; Getter functions
(defn get-pattern [!db pattern-id] @(cursor !db [:pattern-manager :patterns pattern-id]))
(defn saved-patterns [!db] @(cursor !db [:pattern-manager :patterns]))
(defn selected-pattern-id [!db] @(cursor !db [:pattern-manager :selected]))
(defn selected-pattern [!db] (get (saved-patterns !db) (selected-pattern-id !db)))
(defn latest-id [!db] @(cursor !db [:pattern-manager :latest-id]))

(defn export-pattern
  "Given a pattern ID, returns an RLE string interpretation of the pattern."
  [!db id]
  (rle/gen-rle (get-pattern !db id)))

;; Updater functions
(defn pop-id! [!db] (swap! (cursor !db [:pattern-manager :latest-id]) inc))
 

(defn import-pattern!
  "Interprets `rle-str` as a pattern and adds it to the pattern manager in `!db`. Returns the ID of the inserted pattern."
  [!db rle-str]
  (let [id (pop-id! !db)]
    (swap! !db assoc-in [:pattern-manager :patterns id] (rle/parse-rle rle-str))
    id))

(defn select-pattern!
  "Chooses `pattern` as the active selection in the pattern manager in `!db`."
  [!db id]
  (swap! !db assoc-in [:pattern-manager :selected] id))

(defn use-pattern!
  [!db {:keys [board]}]
  (board/push-board! !db board))

(defn use-selected-pattern!
  "Uses the currently-selected pattern, displaying it on the screen."
  [!db]
  (use-pattern! !db (selected-pattern !db)))

(defn select-and-use-pattern!
  "Chooses `pattern` as the active selection in the pattern manager in `!db` and displays it on the screen."
  [!db id]
  (select-pattern! !db id)
  (use-selected-pattern! !db))

(defn import-select-and-use-pattern!
  "Imports `rle-str`, selects it, and displays it on the screen."
  [!db rle-str]
  (let [id (import-pattern! !db rle-str)]
    (select-and-use-pattern! !db id)))