(ns life.rle
 (:require [clojure.string :as string]))

; handles encoding/decoding RLE (run-length-encoded) Life patterns.

; rle takes the following form:
;
; x = m, y = n
; <pattern>
;
; where <pattern> is of form (<run_count>?<tag>)+
; when run_count is unspecified, it defaults to 1.
; The sum(run_count) should equal m * n
; the tag can be: b (dead cell), o (alive cell), $ (eol)
; the b at the end of line, and the $ eol, are optional.
; the last character in the pattern should be !

(def tags {"o" :alive
           "b" :dead
           "$" :eol
           "!" :eop})

(defn parse-int
 [string default]
 (let [parsed (js/parseInt string 10)]
  (if (js/isNaN parsed) default parsed)))

(defn parse-rle-pattern
 "Parses an RLE pattern into its \"AST\": a seq of HashMaps, one per token.
  Note that this is not a perfect parser in that if a pattern has numbers before the ! (e.g. b12!),
  that will pass (but the numbers will be ignored.)"
 [rle-pattern]
 (->> rle-pattern
  (re-seq #"\s*(?:(\d*)([bo$!]))")
  (map (fn [[m c t]] {:tag (tags t) :count (parse-int c 1)}))))

(defn parse-rle-head
 [rle-head]
 (let [[m x y] (re-find #"x = (\d+), y = (\d+)" rle-head)]
  [x y]))

(defn split-rle-str [rle-str]
 (filter #(->> % (first) (= "#") (not)) (string/split-lines rle-str)))




; TODO -- this doesn't handle missing $ characters, I think?
(defn rle->board
 "Given an RLE string, converts it into a Board (HashSet of x/y pairs).
  Note that RLE is defined as reading left-to-right and top-to-bottom,
  whereas in our coordinate system things are numbered bottom-to-top. As
  a result, the patterns are mirrored around the y-axis, and will appear
  to the bottom right of the origin."
 [rle-str]
 (let [[rle-head rle-pattern] (split-rle-str rle-str)
       [width height] (parse-rle-head rle-head)
       tag-list (parse-rle-pattern rle-pattern)
       reducer (fn [{:keys [x y board] :as acc} {:keys [tag count]}]
                (let [is-tile-tag? (contains? #{:alive :dead} tag)]
                 (if (= tag :eoc) acc
                  {:board (if (= tag :alive) (->> x (+ count) (range x) (map (fn [x] [x (- y)])) (into board)) board)
                   :x (if is-tile-tag? (+ x count) 0)
                   :y (if (= tag :eol) (+ y 1) y)})))]
  (:board (reduce reducer {:x 0 :y 0 :board #{}} tag-list))))