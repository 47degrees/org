(ns org.core
  (:require
   [rum.core :as rum]))

(rum/defc app < rum/reactive
  [state]
  (let [{:keys [organization]} (rum/react state)]
    [:h1 organization]))
