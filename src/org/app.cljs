(ns org.app
  (:require
   [org.core :as org]
   [org.client :as c]
   [clojure.set :as set]
   [promesa.core :as p]
   [rum.core :as rum]))

(def configuration
  #=(clojure.edn/read-string (clojure.core/slurp (clojure.java.io/resource "config.edn"))))

(def default-state
  {:filter-language nil
   :order :stars
   :query ""
   :configuration configuration})

(defonce state
  (atom default-state))

(defn init!
  [state]
  (let [{:keys [configuration]} @state
        {:keys [organization
                token
                languages]} configuration]
    (p/then (c/fetch-org-repos! organization {:token token
                                              :languages languages})
            (fn [repos]
              (swap! state assoc :repos repos)
              (rum/mount (org/app state) (js/document.getElementById "app"))))))

(init! state)


