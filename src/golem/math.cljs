(ns golem.math)

(defn ceil [x] (.ceil js/Math x))
(defn floor [x] (.floor js/Math x))