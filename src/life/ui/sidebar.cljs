(ns life.ui.sidebar
 (:require [reagent.ratom :refer [cursor]]
           [life.interop.rle :as rle]
           [life.pattern-manager :as pattern-manager]
           [life.pattern :as pattern]))  
 

(defn is-open?
 [!db]
 @(cursor !db [:ui :sidebar :open]))

(defn toggle! [!db] (swap! !db update-in [:ui :sidebar :open] not))

(defn component
 [!db]
 (let [patterns (pattern-manager/saved-patterns !db)]
  (into [:div.sidebar [:h1 "Pattern Library"]]
   (for [{:keys [name board] {[dim-x dim-y] :dimensions pattern-str :pattern} :rle :as pattern} patterns]
    [:div.saved-board {:on-click #(pattern/use-pattern! !db pattern)}
     [:div.name name]
     [:div.coords (str "x: " dim-x ", y: " dim-y)]
     [:div.rle pattern-str]]))))
   