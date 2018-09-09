(ns golem.util
  "Defines functions and types that are reusable, but not large enough to warrant their own modules.

   Most of the functions just wrap existing JS functions to add a more convenient API."
  (:require [cljs.spec.alpha :as s]))

; this is true for the `figwheel` build and false for the `min` build.
; note -- use ^boolean hints so all debug code is stripped out by tree shaking
(def ^boolean debug? ^boolean js/goog.DEBUG)

(s/def ::coord (s/tuple int? int?))

(defn ceil [x] (.ceil js/Math x))
(defn floor [x] (.floor js/Math x))

(defn parse-int
  ([string] (parse-int string nil))
  ([string default]
   (let [parsed (js/parseInt string 10)]
     (if (js/isNaN parsed) default parsed))))


(defn clear-interval!
  "If the value at `path` in `!db` is a number, call `clearInterval` on it and set it to nil."
  [!db path]
  (swap! !db update-in path #(when (number? %) (js/clearInterval %) nil)))

(defn set-interval!
  "Runs `function` every `interval` ms. Replaces the value at `path` in `!db` to the interval ID.
   If the prior value was an interval ID, the corresponding interval will automatically be cleared."
  [!db path interval function]
  (swap! !db update-in path
         (fn [old-interval-id]
           (when (number? old-interval-id)
             (js/clearInterval old-interval-id))
           (js/setInterval function interval))))