(ns org.app
  (:require
   [cljs.reader :as reader]
   [org.core :as org]
   [org.client :as c]
   [org.state :as st]
   [promesa.core :as p]
   [rum.core :as rum]
   [cljsjs.google-analytics]))

(defn read-state!
  []
  (let [app (js/document.getElementById "app")
        raw (js/atob (.getAttribute app "data-state"))]
    (merge st/default-state (reader/read-string raw))))

(defn init!
  []
  (let [state (atom (read-state!))
        {:keys [organization token extra-repos analytics] :as config} (:config @state)]
    ;; turn on analytics if configured
    (when analytics
      (js/ga "create" analytics "auto")
      (js/ga "send" "pageview"))
    ;; mount app
    (rum/mount (org/app state) (js/document.getElementById "app"))
    ;; fetch fresh data
    (p/then (c/fetch-org-and-extra-repos! organization config)
            (fn [repos]
              (swap! state assoc :repos repos)))))

(defonce main
  (do
    (enable-console-print!)
    (init!)))

(defn on-js-reload
  []
  (println :reloaded))
