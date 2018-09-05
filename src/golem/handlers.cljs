(ns golem.handlers
  (:require [golem.db :refer [!app-db]]))

(defn clear-interval! [key]
  (swap! !app-db update-in [:handlers :interval key] #(when (number? %) (js/clearInterval %) nil)))

(defn set-interval! [key interval function]
  (swap! !app-db update-in [:handlers :interval key]
         (fn [old-interval-id]
           (when (number? old-interval-id)
             (js/clearInterval old-interval-id))
           (js/setInterval function interval))))