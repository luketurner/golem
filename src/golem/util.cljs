(ns golem.util)


(defn parse-int
  ([string] (parse-int string nil))
  ([string default]
   (let [parsed (js/parseInt string 10)]
     (if (js/isNaN parsed) default parsed))))