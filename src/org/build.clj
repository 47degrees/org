(ns org.build
  (:require
   [org.config]
   [clojure.spec.alpha :as s]
   [org.state :as st]
   [org.core :as org]
   [org.client :as c]
   [cljs.build.api :as cljs]
   [cuerdas.core :as str]
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [rum.core :as rum]))

(defn str->base64
  [s]
  (.encodeToString (java.util.Base64/getEncoder) (.getBytes s)))

(rum/defc page
  [state {:keys [js-root] :or {js-root ""}}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta
     {:name "viewport", :content "width=device-width, initial-scale=1"}]
    [:link
     {:rel "icon",
      :type "image/png"
      :href
      "img/favicon.png"}]
    [:link
     {:rel "stylesheet",
      :href
      "https://cdnjs.cloudflare.com/ajax/libs/octicons/3.5.0/octicons.min.css"}]
    [:link
     {:href
      "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css",
      :rel "stylesheet",
      :integrity
      "sha384-T8Gy5hrqNKT+hzMclPo118YTQO6cYprQmhrYwIiQ/3axmI1hQomh7Ud2hPOy8SP1",
      :crossorigin "anonymous"}]
    [:link
     {:href "css/style.css", :rel "stylesheet", :type "text/css"}]]
   [:body
    [:div {:id "app"
           :data-state (str->base64 (pr-str @state))}
     (org/app state)]
    [:script {:src "/js/google-analytics.js" :type "text/javascript"}]
    [:script {:src (str js-root "org.js") :type "text/javascript"}]]])

(defn compile-sass
  [config]
  (let [{:keys [primary-color
                font]} config
        {:keys [url
                base
                headings
                sizes]} font
        {:keys [light
                regular
                semi-bold
                bold]} sizes
        temp "temp"
        tempsass (str temp ".scss")]
    (let [sass (str/<<
                "$brand-primary: ~{primary-color};"
                ; Font
                "@import url(~{url});"
                "$base-font-family: ~{base};"
                "$headings-font-family: ~{headings};"
                ; Font sizes
                "$font-light: ~{light};"
                "$font-regular: ~{regular};"
                "$font-semi-bold: ~{semi-bold};"
                "$font-bold: ~{bold};"
                ; Import default styles
                "@import 'style.scss';")
          tempsass "temp.scss"
          _ (spit tempsass sass)
          ; NOTE: not passing this via stdin since it behaves differently than when compiling a file
          run (sh "sass" "--sourcemap=none" "-Isass" tempsass)]
      (sh "rm" tempsass)
      (:out run))))

(defn compile-cljs!
  [config]
  (cljs/build "src"
              {:main 'org.app
               :output-to "docs/org.js"
               :optimizations :advanced
               :verbose true}))

(defn render-page
  [repos {:keys [organization token-name] :as config} js-root]
  (let [state (atom (assoc st/default-state :repos repos :config config))
        component (page state {:js-root js-root})]
    (rum/render-html component)))

(defn render-static-page
  [repos config]
  (render-page repos config ""))

(defn render-index-page
  [repos {:keys [organization token-name] :as config}]
  (render-page repos config "js/compiled/"))

(defn read-config!
  [path]
  (clojure.edn/read-string (slurp (clojure.java.io/resource path))))

(defn fetch-data!
  [{:keys [organization token extra-repos]}]
  @(c/fetch-org-and-extra-repos! organization {:token token
                                               :extra-repos extra-repos}))

(defn make-dirs!
  [js css]
  (sh "mkdir" "-p" js)
  (sh "mkdir" "-p" css))

(defn generate-index!
  [file repos config]
   (spit file (render-static-page repos config)))

(defn compile-css!
  [file style-config]
  (sh "touch" file)
  (spit file (compile-sass style-config)))

(defn generate-site!
  [config]
  (let [repos (fetch-data! config)]
    ;; Make dir structure
    (make-dirs! "docs/js" "docs/css")
    ;; Compile CLJS
    (compile-cljs! config)
    ;; Generate HTML
    (generate-index! "docs/index.html" repos config)
    ;; Generate CSS
    (compile-css! "docs/css/style.css" (:style config))
    ;; Copy static assets
    (sh "cp" "resources/public/js/google-analytics.js" "docs/js/")
    (sh "cp" "-R" "resources/public/img" "docs")))

(defn -main
  [& args]
  (let [config (read-config! "config.edn")
        token (get (System/getenv) (str (:token-name config)))
        cfg-with-token (assoc config :token token)]
    (when-not (s/valid? :org/config config)
      (println "Invalid configuration: " (s/explain-str :org/config config))
      (System/exit 1))
    (when-not (s/valid? :org/token token)
      (println "Invalid token, you may have missed setting the environment variable " (str \` (:token-name config) \`))
      (System/exit 1))
    (generate-site! cfg-with-token)))

