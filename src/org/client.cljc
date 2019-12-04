(ns org.client
  (:require
   [org.json :as j]
   [httpurr.client :as http]
   [httpurr.status :as status]
   [urania.core :as u]
   [cuerdas.core :as str]
   [promesa.core :as p]
   #? (:clj
       [httpurr.client.aleph :refer [client]]
       :cljs
       [httpurr.client.xhr :refer [client]])))

#?(:clj
   (defn parse-date
     [raw]
     raw)
   :cljs
   (defn parse-date
     [raw]
     (js/Date.parse raw)))

(defn- rel->keyword
  [rel-str]
  (keyword (nth (str/split rel-str "\"") 1)))

(defn parse-links
  [resp]
  (when-let [link (get-in resp [:headers "link"])]
    (let [split-links (map #(str/split % ";") (str/split link "\n"))]
      (into {}
            (map (fn [[url rel]]
                   [(rel->keyword rel) (str/trim url "<>")]))
            split-links))))

(defn- has-next?
  [resp]
  (contains? (parse-links resp) :next))

;; HTTP requests

(defn repo-url
  [user repo]
  (str "https://api.github.com/repos/" user "/" repo))

(defn org-repos-url
  [org]
  (str "https://api.github.com/orgs/" org "/repos"))

(defn headers
  [token]
  {#?@(:clj ["user-agent" "Smith"])
   "accept" "application/vnd.github.v3+json"
   "authorization" (str "Token " token)})

(defn make-repo
  [{:keys [id
           name
           description
           owner
           fork
           pushed_at
           html_url
           languages_url
           contributors_url
           stargazers_count
           watchers_count
           forks_count]}]
  {:id id
   :name name
   :fork fork
   :description description
   :owner (get owner :login)
   :updated (parse-date pushed_at)
   :url html_url
   :stars stargazers_count
   :watchers watchers_count
   :forks forks_count
   :languages-url languages_url
   :contributors-url contributors_url})

(defn parse-repo-response
  [resp]
  (->> resp
    :body
    #?(:clj
       (slurp))
    (j/json->clj)
    make-repo))

(defn parse-repos-response
  [resp]
  (as-> (:body  resp) $
    #?(:clj
       (slurp $))
    (j/json->clj $ {:keywordize? true :default []})
    (mapv make-repo $)))

(defn get-org-repos-next!
  [token repos resp]
  (let [links (parse-links resp)
        req {:method :get
             :url (:next links)
             :headers (headers token)}
        prom (http/send! client req)]
    (p/then prom
            (fn [resp]
              (if (status/success? resp)
                (let [result (into repos (parse-repos-response resp))]
                  (if (has-next? resp)
                    (get-org-repos-next! token result resp)
                    result))
                (p/rejected (ex-info "Unsuccessful request" {:response resp})))))))

(defn get-repo!
  [user repo token]
  (let [req {:method :get
             :url (repo-url user repo)
             :headers (headers token)}
        prom (http/send! client req)]
    (p/then prom
            (fn [resp]
              (if (status/success? resp)
                (parse-repo-response resp)
                (p/rejected (ex-info "Unsuccessful request" {:response resp})))))))

(defn get-org-repos!
  [org token]
  (let [req {:method :get
             :url (org-repos-url org)
             :query-string "type=public&per_page=100"
             :headers (headers token)}
        prom (http/send! client req)]
    (p/then prom
            (fn [resp]
              (if (status/success? resp)
                (let [result (parse-repos-response resp)]
                  (if (has-next? resp)
                    (get-org-repos-next! token result resp)
                    result))
                (p/rejected (ex-info "Unsuccessful request" {:response resp})))))))

(defn parse-languages-response
  [resp]
  (as-> (:body resp) $
        #?(:clj
           (slurp $))
        (j/json->clj $ {:keywordize? false :default {}})
        (set (keys $))))

(defn get-repo-languages!
  [repo token]
  (let [req {:method :get
             :url (get repo :languages-url)
             :query-string "type=public"
             :headers (headers token)}
        prom (http/send! client req)]
    (p/then  prom
             (fn [resp]
               (if (status/success? resp)
                 (parse-languages-response resp)
                 (p/rejected (ex-info "Unsuccessful request" {:response resp})))))))

(defn parse-contributors-response
  [resp]
  (as-> (:body resp) $
        #?(:clj
           (slurp $))
        (j/json->clj $ {:keywordize? true :default []})
        (map :id $)))

(defn get-repo-contributors-next!
  [token contribs links]
  (let [req {:method :get
             :url (:next links)
             :headers (headers token)}
        prom (http/send! client req)]
    (p/mapcat (fn [resp]
                (if (status/success? resp)
                  (let [result (into contribs (parse-contributors-response resp))]
                    (if (has-next? resp)
                      (get-repo-contributors-next! token result (parse-links resp))
                      (p/resolved result)))
                  (p/rejected (ex-info "Unsuccessful request" {:response resp}))))
              prom)))

(defn get-repo-contributors!
  [repo token]
  (let [req {:method :get
             :url (get repo :contributors-url)
             :query-string "type=public&per_page=100"
             :headers (headers token)}
        prom (http/send! client req)]
    (p/mapcat (fn [resp]
               (if (status/success? resp)
                 (let [result (parse-contributors-response resp)]
                   (if (has-next? resp)
                     (get-repo-contributors-next! token result (parse-links resp))
                     (p/resolved result)))
                 (p/rejected (ex-info "Unsuccessful request" {:response resp}))))
             prom)))

;; Data sources

(deftype Repo [user repo]
  u/DataSource
  (-identity [_]
    [:repo [user repo]])
  (-fetch [_ {:keys [token]}]
    (get-repo! user repo token)))

(deftype Repos [org]
  u/DataSource
  (-identity [_]
    [:repos org])
  (-fetch [_ {:keys [token]}]
    (p/then (get-org-repos! org token)
            #(filter (comp not :fork) %))))

(deftype Languages [repo]
  u/DataSource
  (-identity [_]
    [:languages (:name repo)])
  (-fetch [_ {:keys [token]}]
    (get-repo-languages! repo token)))

(deftype Contributors [repo]
  u/DataSource
  (-identity [_]
    [:contributors (:name repo)])
  (-fetch [_ {:keys [token]}]
    (get-repo-contributors! repo token)))

(defn- fetch-languages
  [repo]
  (Languages. repo))

(defn- fetch-contributors
  [repo]
  (Contributors. repo))

(defn- fetch-languages-and-contribs
  [repo]
  (u/map
   (fn [[languages contributors]]
     (assoc repo :languages languages :contributors contributors))
   (u/collect [(fetch-languages repo)
               (fetch-contributors repo)])))

(defn- fetch-org-repos
  [organization]
  (u/traverse
   fetch-languages-and-contribs
   (Repos. organization)))

(defn- fetch-repos
  [repos]
  (u/traverse
   fetch-languages-and-contribs
   (u/collect
    (mapv (fn [{:keys [user repo]}]
            (Repo. user repo))
          repos))))

(defn fetch-org-and-extra-repos
  [org {:keys [token extra-repos]}]
  (u/collect [(fetch-org-repos org)
              (fetch-repos extra-repos)]))

(defn fetch-org-and-extra-repos!
  [org {:keys [token] :as config}]
  (p/then (u/run! (fetch-org-and-extra-repos org config) {:env {:token token}})
          (fn [[org-repos extra-repos]]
            (concat org-repos extra-repos))))
