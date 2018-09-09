(ns golem.ui.viewport.cursors
  (:require [reagent.ratom :refer [cursor]]))


(defn viewport [!db] (cursor !db [:ui :viewport]))


(defn offset [!viewport] (cursor !viewport [:offset]))
(defn scale [!viewport] (cursor !viewport [:scale]))
(defn window [!viewport] (cursor !viewport [:window]))
