(ns golem.util)

(s/def ::coord (s/tuple int? int?))

(defn ceil [x] (.ceil js/Math x))
(defn floor [x] (.floor js/Math x))

(defn parse-int
  ([string] (parse-int string nil))
  ([string default]
   (let [parsed (js/parseInt string 10)]
     (if (js/isNaN parsed) default parsed))))