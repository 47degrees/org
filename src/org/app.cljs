(ns org.app
  (:require
   [cljs.reader :as reader]
   [org.core :as org]
   [org.client :as c]
   [clojure.set :as set]
   [promesa.core :as p]
   [rum.core :as rum]))

(defn read-config!
  []
  (let [app (js/document.getElementById "app")
        raw (js/atob (.getAttribute app "data-configuration"))]
    (reader/read-string raw)))

(defn read-state!
  []
  (let [app (js/document.getElementById "app")
        raw (js/atob (.getAttribute app "data-state"))]
    (reader/read-string raw)))

(defn init!
  []
  (let [config (read-config!)
        initial-state (read-state!)
        state (atom initial-state)
        {:keys [organization
                token
                languages]} config]
    ;; mount app
    (rum/mount (org/app state) (js/document.getElementById "app"))
    ;; fetch fresh data
    (p/then (c/fetch-org-repos! organization {:token token})
            (fn [repos]
              (swap! state assoc :repos repos)))))

(enable-console-print!)
(init!)


