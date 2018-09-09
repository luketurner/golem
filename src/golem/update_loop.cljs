(ns golem.update_loop
  (:require [reagent.ratom :refer [run! cursor]]
            [cljs.spec.alpha :as s]
            [golem.util :as util]))

(s/def ::interval pos-int?)
(s/def ::interval-id (s/nilable int?))
(s/def ::enabled boolean?)
(s/def ::update-loop (s/keys :req-un [::interval
                                      ::enabled
                                      ::interval-id]))

(def default-state {:interval 100
                    :enabled  true
                    :interval-id nil})

;; Getter functions
(defn is-enabled? [!db] @(cursor !db [:update-loop :enabled]))
(defn get-rate [!db] @(cursor !db [:update-loop :interval]))

;; Updater functions
(defn disable! [!db] (swap! !db assoc-in [:update-loop :enabled] false))
(defn enable! [!db] (swap! !db assoc-in [:update-loop :enabled] true))
(defn toggle! [!db] (swap! !db update-in [:update-loop :enabled] not))
(defn inc-rate! [!db ms] (swap! !db update-in [:update-loop :interval] #(max 0 (+ % ms))))

(defn run-loop! [!db update-fn]
  (let [!interval (cursor !db [:update-loop :interval])
        !enabled? (cursor !db [:update-loop :enabled])]
    (run!
      (if @!enabled?
        (util/set-interval! !db [:update-loop :interval-id] @!interval update-fn)
        (util/clear-interval! !db [:update-loop :interval-id])))))