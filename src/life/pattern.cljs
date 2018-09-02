(ns life.pattern
 (:require [life.interop.rle :as rle]
           [life.board :as board]))

; A "pattern" is a reusable "board" with added metadata.
; Roughly based on the concept of RLE patterns, but not tied to RLE structure.

(defn rle->pattern
 [rle-str]
 (let [{:keys [name dimensions board offset origin] :as rle} (rle/parse-rle rle-str)]
  {:name (or name "Unnamed Pattern")
   :dimensions dimensions
   :board board
   :offset offset
   :origin origin
   :rle rle}))

(defn use-pattern!
 [!db {:keys [board]}]
 (board/push-board! !db board))