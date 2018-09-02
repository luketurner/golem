(ns life.board-manager
 (:require [reagent.ratom :refer [cursor]]
           [life.board :as board]
           [life.rle :as rle]))

; Module for maintaining a list of saved boards that can be loaded later.
; Used for three things:
; 1. Maintaining list of interesting premade boards for users to choose from.
; 2. Allowing users to import/export boards in RLE format
; 3. Allowing user to save current board (simiar to "bookmarking" it)

; boards are saved as a boardspec: HashMap{:name :rle :board}

(defn rle->boardspec [rle name] {:name (or name "Unnamed Board") :rle rle :board (rle/rle->board rle)})

(def default-premades
 [(rle->boardspec "x = 3, y = 3\nbo$2bo$3o!" "Glider")])

(def default-state {:selected (first default-premades)
                    :saved default-premades})

(defn get-saved [!db] @(cursor !db [:board-manager :saved]))
(defn get-selected-board [!db] @(cursor !db [:board-manager :selected :board]))

(defn push-selected-board!
 "Immediately calls push-board! on the currently selected board in the board manager.
  Effectively resets the board state to the last-chosen board."
 [!db]
 (board/push-board! !db (get-selected-board !db)))