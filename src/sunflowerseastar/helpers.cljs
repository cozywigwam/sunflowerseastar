(ns sunflowerseastar.helpers
  (:require [reagent.core :refer [create-class dom-node]]))

(defn highlight-code [el]
  (let [nodes (.querySelectorAll el "pre code")]
    (loop [i (.-length nodes)]
      (when-not (neg? i)
        (when-let [item (.item nodes i)]
          (.highlightBlock js/hljs item))
        (recur (dec i))))))

(defn math-jax-wrapper [inner]
  (create-class
   {:component-did-mount #(do
                            ;; (.typeset (.-MathJax js/window))
                              (highlight-code (dom-node %)))
    :reagent-render (fn [this] inner)}))