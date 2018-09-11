(ns golem.update_loop
  (:require [reagent.ratom :refer [cursor]]
            [cljs.spec.alpha :as s]
            [golem.util :as util]))

(s/def ::fps pos-int?)
(s/def ::interval-id (s/nilable int?))
(s/def ::enabled boolean?)
(s/def ::update-loop (s/keys :req-un [::fps
                                      ::enabled
                                      ::interval-id]))

(def default-state {:fps         15
                    :enabled     true
                    :interval-id nil})

;; Cursors
(defn is-enabled? [!db] (cursor !db [:update-loop :enabled]))
(defn fps [!db] (cursor !db [:update-loop :fps]))
(defn- interval-id [!db] (cursor !db [:update-loop :interval-id]))

;; Updater functions
(defn disable! [!db] (reset! (is-enabled? !db) false))
(defn enable! [!db] (reset! (is-enabled? !db) true))
(defn toggle! [!db] (swap! (is-enabled? !db) not))
(defn inc-fps! [!db n] (swap! (fps !db) #(min 60 (+ % n))))

(defn run-loop! [!db update-fn]
  (let [!fps (fps !db)
        !enabled? (is-enabled? !db)]
    (util/run-once! :update-loop
      (if @!enabled?
        (util/set-interval! (interval-id !db) (util/ceil (/ 1000 @!fps)) update-fn)
        (util/clear-interval! (interval-id !db))))))