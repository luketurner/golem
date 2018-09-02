(ns life.ui.sidebar
 (:require [reagent.ratom :refer [cursor]]
           [life.rle :as rle]
           [life.board-manager :as board-manager]
           [life.board :as board]))  
 

(defn is-open?
 [!db]
 @(cursor !db [:ui :sidebar :open]))

(defn toggle! [!db] (swap! !db update-in [:ui :sidebar :open] not))

(defn component
 [!db]
 (let [saved-boards (board-manager/get-saved !db)]
  (into [:div.sidebar [:h1 "Saved Boards"]]
   (for [{:keys [name rle board]} saved-boards]
    (let [[rle-head rle-pattern] (rle/split-rle-str rle)]
     [:div.saved-board {:on-click #(board/push-board! !db board)}
      [:div.name name]
      [:div.coords rle-head]
      [:div.rle rle-pattern]])))))
   