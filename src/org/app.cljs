(ns org.app
  (:require
   [org.core :as org]
   [org.client :as c]
   [clojure.set :as set]
   [promesa.core :as p]
   [rum.core :as rum]))

(def default-state
  #=(clojure.edn/read-string (clojure.core/slurp (clojure.java.io/resource "config.edn"))))

(defonce state
  (atom default-state))

(defn init!
  [state]
  (let [{:keys [organization
                projects
                languages
                token]} @state]
    (p/then (c/fetch-org-repos! organization {:token token
                                              :projects projects
                                              :languages languages})
            (fn [repos]
              (swap! state assoc :repos repos)))
    (rum/mount (org/app state) (js/document.getElementById "app"))))

(init! state)


