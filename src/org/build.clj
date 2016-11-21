(ns org.build
  (:require
   [org.state :as st]
   [org.core :as org]
   [org.client :as c]
   [cljs.build.api :as cljs]
   [cuerdas.core :as str]
   [cuerdas.regexp :as regexp]
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [rum.core :as rum]))

(defn str->base64
  [s]
  (.encodeToString (java.util.Base64/getEncoder) (.getBytes s)))

(rum/defc page
  [state]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta
     {:name "viewport", :content "width=device-width, initial-scale=1"}]
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
    [:script {:src "org.js" :type "text/javascript"}]]])

(defn compile-css
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

(defn render-static-page
  [repos {:keys [organization token] :as config}]
  (let [state (atom (assoc st/default-state :repos repos :config config))
        component (page state)]
    (rum/render-html component)))

(defn render-blank-page
  [{:keys [organization token] :as config}]
  (let [state (atom (assoc st/default-state :config config))
        component (page state)]
    (rum/render-static-markup component)))

(defn read-config!
  [path]
  (clojure.edn/read-string (slurp (clojure.java.io/resource path))))

(defn fetch-data!
  [{:keys [organization token]}]
  @(c/fetch-org-repos! organization {:token token}))

(defn -main
  [& args]
  (let [config (read-config! "config.edn")
        repos (fetch-data! config)
        html (render-static-page repos config)
        style-config (get config :style)
        css (compile-css style-config)]
    (sh "mkdir" "-p" "docs/js")
    (sh "mkdir" "-p" "docs/css")
    (println "Compiling ClojureScript..")
    (compile-cljs! config)
    (spit "docs/index.html" html)
    (sh "touch" "docs/css/style.css")
    (sh "cp" "-R" "resources/public/img" "docs")
    (spit "docs/css/style.css" css)))

