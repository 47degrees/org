(ns org.build
  (:require
   [rum.core :as rum]
   [org.core :as org]
   [org.client :as c]))


(defn compile-css
  [config]
  (let [{:keys [font
                font-size
                header-font-size
                logo]} config]
    ;; todo: spit a .scss file that
    ;; - imports the main stylesheet
    ;; - declares config as variables overriding the defaults
    ;; - outputs the css to resources/public/css/style.css
    
    ))

(defn compile-cljs
  [config]
  )

(defn render-static-page
  [config]
  )

;; todo: config spec

(rum/defc page
  [state]
  [:html
   [:head]
   [:body
    [:div {:id "app"}
     (org/app state)]]])

(defn build
  [{:keys [organization token projects languages] :as config}]
  (let [repos (c/fetch-org-repos! organization {:token token
                                                :projects projects
                                                :languages languages})
        state (atom {:organization organization
                     :projects projects
                     :languages languages
                     :repos repos})
        component (page state)]
    (rum/render-html component)))

(comment
  (bu/build {:organization "funcool"
             :token token
             :projects #{"httpurr" "urania"}
             :languages #{"Clojure"}})
  )

