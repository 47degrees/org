(ns org.server
  (:require
   [cuerdas.core :as str]
   [org.build :as b]))

(defn handler
  [{:keys [uri]}]
  (when (or (= uri "/")
            (= uri "/index.html"))
    (let [config (b/read-config! "config.edn")
          repos (b/fetch-data! config)]
      {:status 200
       :headers {"content-type" "text/html"}
       :body (b/render-index-page repos config)})))
