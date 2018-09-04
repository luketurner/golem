(ns life.pattern
 (:require [life.interop.rle :as rle]
           [life.board :as board]))

; A "pattern" is a reusable "board" with added metadata.
; Roughly based on the concept of RLE patterns, but not tied to RLE structure.

(defn rle->pattern [rle-str] (rle/parse-rle rle-str))

(defn pattern->rle [pattern] (rle/gen-rle pattern)) 
      

(defn use-pattern!
 [!db {:keys [board]}]
 (board/push-board! !db board))