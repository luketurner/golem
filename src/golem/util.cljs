(ns golem.util
  "Defines functions and types that are reusable, but not large enough to warrant their own modules.

   Most of the functions just wrap existing JS functions to add a more convenient API."
  (:require [cljs.spec.alpha :as s]
            [reagent.ratom :refer [make-reaction dispose!]]))

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


;; This is some private state, kept separate from the app-db. Not sure if that's a good idea in the long run.
;; Mostly used to facilitate hot-reloading.
(defonce ^:private !intervals (atom {}))
(defonce ^:private !reactions (atom {}))

(defn clear-interval!
  "If the value in `!atom` a number, call `clearInterval` on it and set it to nil."
  [key]
  (swap! !intervals update key #(when (number? %) (js/clearInterval %) nil)))

(defn set-interval!
  "Runs `function` every `interval` ms. Replaces the value in `!atom` to the interval ID.
   If the prior value was an interval ID, the corresponding interval will automatically be cleared."
  [key interval f]
  (swap! !intervals update key
         (fn [old-interval-id]
           (when (number? old-interval-id)
             (js/clearInterval old-interval-id))
           (js/setInterval f interval))))

(defn singleton-reaction
  "Creates and returns reaction for `f`. If called more than once with the same `key`, earlier versions of the reaction
   will be disposed, ensuring only one reaction is watching at any time."
  [key f]
  (let [reaction (make-reaction f :auto-run true)]
    (swap! !reactions update key (fn [old] (when (some? old) (dispose! old)) reaction))
    reaction))

 
 