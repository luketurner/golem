(ns golem.math
  (:require [cljs.spec.alpha :as s]))

(s/def ::coord (s/tuple int? int?))

(defn ceil [x] (.ceil js/Math x))
(defn floor [x] (.floor js/Math x))