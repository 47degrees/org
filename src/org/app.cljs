(ns org.app
  (:require
   [org.core :as org]
   [org.client :as c]
   [org.state :as st]
   [clojure.set :as set]
   [promesa.core :as p]
   [rum.core :as rum]))

(defonce state
  (atom st/default-state))

(defn init!
  [state]
  (let [{:keys [configuration]} @state
        {:keys [organization
                token
                languages]} configuration]
    (p/then (c/fetch-org-repos! organization {:token token})
            (fn [repos]
              (swap! state assoc :repos repos)
              (rum/mount (org/app state) (js/document.getElementById "app"))))))

(init! state)


