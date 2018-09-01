(ns life.update-loop
    (:require [reagent.ratom :refer [run! cursor]]
              [life.handlers :refer [set-interval! clear-interval!]]))


(defn run-loop! [!app-db update-fn]
 (let [!interval (cursor !app-db [:updater :interval])
       !enabled? (cursor !app-db [:updater :enabled])]
  (run!
   (if @!enabled?
    (set-interval! :update-loop @!interval update-fn)
    (clear-interval! :update-loop)))))