(ns golem.update_loop
  (:require [reagent.ratom :refer [run! cursor]]
            [golem.handlers :refer [set-interval! clear-interval!]]
            [cljs.spec.alpha :as s]))

(s/def ::update-loop (s/keys :req-un []))
(s/def ::interval pos-int?)
(s/def ::enabled boolean?)

(def default-state {:interval 100
                    :enabled  true})

(defn is-enabled? [!db] @(cursor !db [:update-loop :enabled]))
(defn get-rate [!db] @(cursor !db [:update-loop :interval]))

(defn disable! [!db]
  (swap! !db assoc-in [:update-loop :enabled] false))

(defn enable! [!db]
  (swap! !db assoc-in [:update-loop :enabled] true))

(defn toggle! [!db]
  (swap! !db update-in [:update-loop :enabled] not))

(defn inc-rate! [!db ms]
  (swap! !db update-in [:update-loop :interval] #(max 0 (+ % ms))))

(defn with-timer [tag func]
  #(time (func)))

(defn run-loop! [!db update-fn]
  (let [!interval (cursor !db [:update-loop :interval])
        !enabled? (cursor !db [:update-loop :enabled])]
    (run!
      (if @!enabled?
        (set-interval! !db :update-loop @!interval update-fn)
        (clear-interval! !db :update-loop)))))