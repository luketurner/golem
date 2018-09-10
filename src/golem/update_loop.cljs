(ns golem.update_loop
  (:require [reagent.ratom :refer [run! cursor]]
            [cljs.spec.alpha :as s]
            [golem.util :as util]))

(s/def ::fps pos-int?)
(s/def ::interval-id (s/nilable int?))
(s/def ::enabled boolean?)
(s/def ::update-loop (s/keys :req-un [::fps
                                      ::enabled
                                      ::interval-id]))

(def default-state {:fps 60
                    :enabled  true
                    :interval-id nil})

;; Getter functions
(defn is-enabled? [!db] @(cursor !db [:update-loop :enabled]))
(defn get-fps [!db] @(cursor !db [:update-loop :fps]))

;; Updater functions
(defn disable! [!db] (swap! !db assoc-in [:update-loop :enabled] false))
(defn enable! [!db] (swap! !db assoc-in [:update-loop :enabled] true))
(defn toggle! [!db] (swap! !db update-in [:update-loop :enabled] not))
(defn inc-fps! [!db fps] (swap! !db update-in [:update-loop :fps] #(min 60 (+ % fps))))

(defn run-loop! [!db update-fn]
  (let [!fps (cursor !db [:update-loop :fps])
        !enabled? (cursor !db [:update-loop :enabled])]
    (run!
      (if @!enabled?
        (util/set-interval! !db [:update-loop :interval-id] (util/ceil (/ 1000 @!fps)) update-fn)
        (util/clear-interval! !db [:update-loop :interval-id])))))