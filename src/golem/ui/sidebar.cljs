(ns golem.ui.sidebar
  (:require [reagent.ratom :refer [cursor]]
            [golem.pattern_manager :as pattern-manager]
            [cljs.spec.alpha :as s]))

(s/def ::sidebar (s/keys :req-un [::open ::import-ref ::export-ref]))
(s/def ::open boolean?)
(s/def ::import-ref (s/nilable (partial instance? js/HTMLElement)))
(s/def ::export-ref (s/nilable (partial instance? js/HTMLElement)))


(def default-state {:open false
                    :import-ref nil
                    :export-ref nil})

(defn is-open?
  [!db]
  @(cursor !db [:ui :sidebar :open]))

(defn toggle! [!db] (swap! !db update-in [:ui :sidebar :open] not))
(defn set-import-ref! [!db ref] (swap! !db assoc-in [:ui :sidebar :import-ref] ref))
(defn set-export-ref! [!db ref] (swap! !db assoc-in [:ui :sidebar :export-ref] ref))
(defn get-import-ref [!db] @(cursor !db [:ui :sidebar :import-ref]))
(defn get-export-ref [!db] @(cursor !db [:ui :sidebar :export-ref]))
(defn import-field-content! [!db]
  (when-let [el (get-import-ref !db)]
    (->> el (.-value) (pattern-manager/import-select-and-use-pattern! !db))))

(defn select-export-text!
  [!db]
  (when-let [el (get-export-ref !db)]
    (.focus el)
    (.select el)))

(defn component
  [!db]
  (let [patterns (pattern-manager/saved-patterns !db)
        selected-pattern-id (pattern-manager/selected-pattern-id !db)]
    (vec
      (concat [:div.sidebar [:h1.sidebar-header "Pattern Library"]]
              [(into [:div.sidebar-body (for [[id {:keys [name board] [dim-x dim-y] :dimensions pattern-str :pattern :as pattern}] patterns]
                                          [:div.saved-board {:class (when (= id selected-pattern-id) "selected")
                                                             :on-click #(pattern-manager/select-and-use-pattern! !db id)
                                                             :key id}
                                            [:div.name name]
                                            [:div.coords (str "x: " dim-x ", y: " dim-y)]
                                            [:div.rle pattern-str]])])]
              [[:div.sidebar-footer
                [:label "Import from RLE"]
                [:textarea.import-field {:placeholder "Paste RLE to import..." :ref #(set-import-ref! !db %)}]
                [:button {:on-click #(import-field-content! !db)} "Import RLE"]
                [:label "Export to RLE"]
                [:textarea.export-field {:read-only true :value (pattern-manager/export-pattern !db selected-pattern-id) :ref #(set-export-ref! !db %)}]
                [:button {:on-click #(select-export-text! !db)} "Select All"]]]))))
   
   