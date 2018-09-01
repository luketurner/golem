(ns life.db
    (:require [reagent.core :as reagent :refer [atom]]))

(defonce !app-db
 (atom
  {:board #{}
   :updater {:interval 500
             :enabled true}
   :handlers {:interval {}}
   :viewport {:canvas nil
              :window [100 100]
              :offset [0 0]
              :scale 1.0}}))