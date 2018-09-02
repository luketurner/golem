(ns life.interop.rle
 (:require [clojure.string :as string]))

; handles encoding/decoding RLE (run-length-encoded) Life patterns.
; See: http://www.conwaylife.com/w/index.php?title=Run_Length_Encoded

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
  [(js/parseInt x 10) (js/parseInt y 10)]))

(defn split-rle-str [rle-str]
 (filter #(->> % (first) (= "#") (not)) (string/split-lines rle-str)))

(defn parse-rle-comments
 [rle-data rle-str]
 [])

(defn get-#-value [line] (string/trim (.slice line 2)))

(defn standardize-pattern [pattern] (string/replace pattern #"\s+" ""))

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

; TODO -- this doesn't handle missing $ characters, I think?
(defn pattern-ast->board
 "Given an RLE string, converts it into a Board (HashSet of x/y pairs).
  Note that RLE is defined as reading left-to-right and top-to-bottom,
  whereas in our coordinate system things are numbered bottom-to-top. As
  a result, the patterns are mirrored around the y-axis, and will appear
  to the bottom right of the origin."
 [pattern-ast]
 (let [reducer (fn [{:keys [x y board] :as acc} {:keys [tag count]}]
                (let [is-tile-tag? (contains? #{:alive :dead} tag)]
                 (if (= tag :eoc) acc
                  {:board (if (= tag :alive) (->> x (+ count) (range x) (map (fn [x] [x (- y)])) (into board)) board)
                   :x (if is-tile-tag? (+ x count) 0)
                   :y (if (= tag :eol) (+ y 1) y)})))]
  (:board (reduce reducer {:x 0 :y 0 :board #{}} pattern-ast))))


(defn parse-rle
 "Given an RLE string, returns a parsed RLE object representing it.
  Note -- this is slightly more lenient than it could be. For example,
  # lines can be interleaved into the pattern with no ill effects. However,
  it should at least be enough to parse the usual RLE out there."
 [rle-str]
 (loop [lines (string/split-lines rle-str)
        data {}]
  (if-let [line (first lines)]
   (recur
    (rest lines)

    ; update data object accordingly, depending on what type of line we're dealing with.
    ; use the first 2 characters of the line to identify the type.
    (case (.slice line 0 2)

     ; In this case, the line was originally empty. We can ignore it.
     "" data

     ; #P or #R lists the coords of the top left corner of the pattern. e.g. #P 12 32
     ("#P" "#R") (assoc data :offset (->> line 
                                          (get-#-value)
                                          (#(string/split % " "))
                                          (map #(js/parseInt % 10))
                                          (into [])))
     
     ; #C is a comment line
     ("#C" "#c") (update data :comments #(conj (or % []) (get-#-value line)))

     ; #N is name
     "#N" (assoc data :name (get-#-value line))

     ; #O is the origin (e.g. author and created-at date)
     "#O" (assoc data :origin (get-#-value line))

     ; If the line starts with this, it's the "x = m, y = n" line
     "x " (assoc data :dimensions (parse-rle-head line))

     ; otherwise, assume the line is part of the pattern, and add it.
     (update data :pattern #(str % (standardize-pattern line)))))
   
   ; no more lines -- do final calculation steps and return data
   (let [pattern-ast (parse-rle-pattern (:pattern data))
         board (pattern-ast->board pattern-ast)]
    (into data {:pattern-ast pattern-ast :board board})))))
