(ns org.server
  (:require
   [org.build :as b]))

(defn handler
  [{:keys [uri]}]
  (when (or (= uri "/")
            (= uri "/index.html"))
    (let [config (b/read-config! "config.edn")
          token (get (System/getenv) (str (:token-name config)))
          cfg (assoc config :token token)
          repos (b/fetch-data! cfg)]
      {:status 200
       :headers {"content-type" "text/html"}
       :body (b/render-index-page repos cfg)})))
