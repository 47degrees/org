(ns org.config
  #?(:clj
     (:require [cuerdas.core :as str]
               [clojure.spec.alpha :as s])
     :cljs
     (:require [cuerdas.core :as str]
               [cljs.spec.alpha :as s])))

;; Organization

(s/def :org/organization string?)
(s/def :org/organization-name string?)

;; Logo

(s/def :logo/href string?)
(s/def :logo/src string?)
(s/def :org/logo (s/keys :req-un [:logo/href
                                  :logo/src]))

(s/def :org/project-logos (s/map-of string? string?))

;; Links

(s/def :link/text string?)
(s/def :link/href string?)
(s/def :org/link (s/keys :req-un [:link/text
                                  :link/href]))
(s/def :org/links (s/coll-of :org/link))

;; Languages

(s/def :org/language string?)
(s/def :org/languages (s/coll-of :org/language
                                 :kind set?
                                 :min-count 1))

;; Projects

(s/def :org/project string?)
(s/def :org/included-projects (s/coll-of :org/project
                                         :kind set?
                                         :min-count 1))
(s/def :org/archived-projects (s/coll-of :org/project
                                         :kind set?))

(s/def :repo/user string?)
(s/def :repo/repo string?)
(s/def :org/repo (s/keys :req-un [:repo/user
                                  :repo/repo]))

;; Tokens

(s/def :org/token-name string?)
(s/def :org/token (s/and string? (complement str/blank?)))
(s/def :org/analytics string?)

;; Style

(s/def :style/primary-color string?)

(s/def :header/background string?)
(s/def :header/backgroundSize string?)
(s/def :style/header (s/keys :req-un [:header/background]
                             :opt-un [:header/backgroundSize]))


(s/def :font/url string?)
(s/def :font/base string?)
(s/def :font/headings string?)
(s/def :font-size/light int?)
(s/def :font-size/regular int?)
(s/def :font-size/semi-bold int?)
(s/def :font-size/bold int?)
(s/def :font/sizes (s/keys :req-un [:font-size/light
                                    :font-size/regular
                                    :font-size/semi-bold
                                    :font-size/bold]))
(s/def :style/font (s/keys :req-un [:font/url
                                    :font/base
                                    :font/headings
                                    :font/sizes]))

(s/def :org/style (s/keys :req-un [:style/primary-color
                                   :style/header
                                   :style/font]))

;; Social

(s/def :social/twitter string?)
(s/def :social/facebook string?)
(s/def :social/linkedin string?)
(s/def :org/social (s/keys :opt-un [:social/twitter
                                    :social/facebook
                                    :social/linkedin]))

;; Configuration

(s/def :org/config (s/keys :req-un [:org/organization
                                    :org/logo
                                    :org/links
                                    :org/languages
                                    :org/included-projects
                                    :org/archived-projects
                                    :org/token-name
                                    :org/style]
                           :opt-un [:org/organization-name
                                    :org/social
                                    :org/extra-repos
                                    :org/project-logos
                                    :org/analytics]))
