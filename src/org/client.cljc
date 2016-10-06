(ns org.client
  (:require
   [org.json :as j]
   [httpurr.client :as http]
   [httpurr.status :as status]
   [urania.core :as u]
   [promesa.core :as p]
   [org.json :as json]
   #? (:clj
       [httpurr.client.aleph :refer [client]]
       :cljs
       [httpurr.client.xhr :refer [client]])))

;; HTTP requests

(defn org-repos-url
  [org]
  (str "https://api.github.com/orgs/" org "/repos"))

(defn make-repo
  [{:keys [id
           name
           description
           owner
           html_url
           languages_url
           stargazers_count
           watchers_count
           forks_count]}]
  {:id id
   :name name
   :description description
   :owner (get owner :login)
   :url html_url
   :stars stargazers_count
   :watchers watchers_count
   :forks forks_count
   :languages-url languages_url})

(defn parse-repos-response
  [resp]
  (->> resp
    :body
    #?(:clj
       (slurp))
    (j/json->clj)
    (mapv make-repo)))

(defn get-org-repos!
  [org token]
  ;; todo: follow pagination
  (let [req {:method :get
             :url (org-repos-url org)
             :query-string "type=public"
             :headers {#?@(:clj ["user-agent" "Smith"])
                       "accept" "application/vnd.github.v3+json"
                       "authorization" (str "Token " token)}}
        prom (http/send! client req)]
    (p/then  prom
             (fn [resp]
               (if (status/success? resp)
                 (parse-repos-response resp)
                 (p/rejected (ex-info "Unsuccessful request" {:response resp})))))))

(defn repo-langs-url
  [repo]
  (get repo :languages-url))

(defn parse-languages-response
  [resp]
  (as-> (:body resp) $
        #?(:clj
           (slurp $))
        (j/json->clj $ {:keywordize? false})
        (keys $)))

(defn get-repo-languages!
  [repo token]
  (let [req {:method :get
             :url (repo-langs-url repo)
             :query-string "type=public"
             :headers {#?@(:clj ["user-agent" "Agent Smith"])
                       "accept" "application/vnd.github.v3+json"
                       "authorization" (str "Token " token)}}
        prom (http/send! client req)]
    (p/then  prom
             (fn [resp]
               (if (status/success? resp)
                 (parse-languages-response resp)
                 (p/rejected (ex-info "Unsuccessful request" {:response resp})))))))

;; Data sources

(deftype Repos [org]
  u/DataSource
  (-identity [_]
    [:repos org])
  (-fetch [_ {:keys [token]}]
    (get-org-repos! org token)))

(deftype Languages [repo]
  u/DataSource
  (-identity [_]
    [:languages (:name repo)])
  (-fetch [_ {:keys [token]}]
    (get-repo-languages! repo token)))

(defn- fetch-repo-languages
  [repo languages]
  (u/map
   (fn [langs]
     (assoc repo :languages (filter (set languages) langs))) ;; todo: filter relevant langs
     (Languages. repo)))

(defn- fetch-interesting-repos
  [repos projects languages]
  (let [interesting? (set projects)
        interesting-repos (filter #(interesting? (:name %)) repos)]
    (u/traverse
     #(fetch-repo-languages % languages)
     (u/value interesting-repos))))

(defn- fetch-org-repos
  [organization projects languages]
  (u/mapcat
   #(fetch-interesting-repos % projects languages)
   (Repos. organization)))

(defn fetch-org-repos!
  [org {:keys [token projects languages]}]
  (u/run! (fetch-org-repos org projects languages) {:env {:token token}}))
