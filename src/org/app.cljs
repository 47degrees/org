(ns org.app
  (:require
   [org.core :as org]
   [org.client :as c]
   [clojure.set :as set]
   [promesa.core :as p]
   [rum.core :as rum]))

(def default-state
  {:organization "47deg"
   :projects #{"fetch" "mvessel" "macroid" "org" "case-classy" "sbot" "github4s" "second-bridge"}
   :languages #{"Scala" "Clojure" "Java" "Swift"}
   :token "0ea220b5c8de1be060c132e24771ed74537821be"})

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


