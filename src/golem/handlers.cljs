(ns golem.handlers
  (:require [cljs.spec.alpha :as s]))


(s/def ::handlers (s/keys :req-un [::interval]))
(s/def ::interval (s/map-of keyword? (s/nilable int?)))

(def default-state {:interval {}})


(defn clear-interval! [!db key]
  (swap! !db update-in [:handlers :interval key] #(when (number? %) (js/clearInterval %) nil)))

(defn set-interval! [!db key interval function]
  (swap! !db update-in [:handlers :interval key]
         (fn [old-interval-id]
           (when (number? old-interval-id)
             (js/clearInterval old-interval-id))
           (js/setInterval function interval))))