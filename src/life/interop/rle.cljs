(ns life.interop.rle
  (:require [clojure.string :as string]
            [life.util :as util]
            [com.rpl.specter :as s]))

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

(def str->tag {"o" :alive
               "b" :dead
               "$" :eol
               "!" :eop})

(def tag->str (s/transform [s/ALL] reverse str->tag))

(defn parse-rle-pattern
  "Parses an RLE pattern into its \"AST\": a seq of HashMaps, one per token.
   Note that this is not a perfect parser in that if a pattern has numbers before the ! (e.g. b12!),
   that will pass (but the numbers will be ignored.)"
  [rle-pattern]
  (->> rle-pattern
       (re-seq #"\s*(?:(\d*)([bo$!]))")
       (map (fn [[m c t]] {:tag (str->tag t) :count (util/parse-int c 1)}))))

(defn parse-rle-head
  [rle-head]
  (let [[m x y] (re-find #"x = (\d+), y = (\d+)" rle-head)]
    [(util/parse-int x) (util/parse-int y)]))

(defn get-#-value [line] (string/trim (.slice line 2)))

(defn standardize-pattern [pattern] (string/replace pattern #"\s+" ""))

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
  (loop [lines (->> rle-str (string/split-lines) (map string/trim))
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
                                              (map #(util/parse-int %))
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


(defn zip-pairs
  [coll]
  (map vector coll (concat (drop 1 coll) [nil])))


(defn- compact-tag-list
  [tags]
  (->> tags
       (partition-by :tag)
       (map (fn [vs] (reduce (fn [acc {c :count}] (update acc :count #(+ % c))) vs)))))


(defn- x-coords->tags
  [xs]
  (let [tags-reducer (fn [tags [old-x next-x]]
                       (let [between-tiles (- next-x 1 (or old-x 0))]
                         (concat tags
                                 (when (<= 0 old-x) [{:tag :alive :count 1}])
                                 (when (< 0 between-tiles) [{:tag :dead :count between-tiles}]))))]
    (->> xs
         (cons -1)
         (zip-pairs)
         (reduce tags-reducer [])
         (compact-tag-list))))


(defn board->pattern-ast
  [board]

  ; todo -- make this more efficient?
  (->> board                                                ; #{[1 1] [1 3] [1 2] [0 1] [5 1] [1 5]}
       (group-by second)                                    ; {1 ([1 1] [0 1] [5 1]), 3 ([1 3]), 2 ([1 2]), 5 ([1 5])}
       (into {0 '()})                                       ; {0 (), 1 ([1 1] [0 1] [5 1]), 3 ([1 3]), 2 ([1 2]), 5 ([1 5])}
       (s/transform [s/MAP-VALS] #(sort-by first %))        ; {0 (), 1 ([0 1] [1 1] [5 1]), 3 ([1 3]), 2 ([1 2]), 5 ([1 5])}
       (s/transform [s/MAP-VALS s/ALL] first)               ; (0 (), 1 (0 1 5), 3 (1), 2 (1), 5 (1))
       (sort-by first)                                      ; (0 (), 1 (0 1 5), 2 (1), 3 (1), 5 (1))
       (zip-pairs)
       (reduce
         (fn [ast [this-line next-line]]
           (let [[y-coord x-coords] this-line
                 [next-y-coord _] (or next-line [inc y-coord])
                 num-eols (if next-line (- (first next-line) y-coord) 1)]
             (concat ast
                     (x-coords->tags x-coords)
                     (when (pos? num-eols) [{:tag :eol :count num-eols}]))))
         [])
       (vec)
       (#(conj % {:tag :eop :count :1}))))



(defn pattern-ast->str
  "Gconvers an RLE pattern AST (array of tags) into a string"
  [pattern-ast]
  (apply str (for [{:keys [count tag]} pattern-ast] (str (when-not (= count 1) count) (tag->str tag)))))


(defn gen-#N [{:keys [name]}] (str "#N " name "\n"))
(defn gen-#O [{:keys [origin]}] (str "#O " origin "\n"))
(defn gen-#P [{:keys [offset]}] (when (seq offset) (str "#P " (string/join offset " ") "\n")))
(defn gen-#C [{:keys [comments]}] (reduce #(str %1 "#C " %2 "\n") "" comments))
(defn gen-xy [{[x y] :dimensions}] (str "x = " x ", y = " y "\n"))
(defn gen-pattern
  [{:keys [pattern pattern-ast board]}]
  (cond
    pattern pattern
    pattern-ast (pattern-ast->str pattern-ast)
    board (-> board (board->pattern-ast) (pattern-ast->str))))

(defn gen-rle
  "Generates an RLE string based on the RLE AST object passed in."
  [rle-ast]
  (str
    (gen-#N rle-ast)
    (gen-#O rle-ast)
    (gen-#C rle-ast)
    (gen-#P rle-ast)
    (gen-xy rle-ast)
    (gen-pattern rle-ast)))

