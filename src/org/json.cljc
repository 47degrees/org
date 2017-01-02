(ns org.json
  #?(:clj
     (:require [clojure.data.json :as j]
               [cuerdas.core :as str])
     :cljs
     (:require [clojure.walk :as walk]
               [cuerdas.core :as str])))

(defn json->clj
  ([json]
   (json->clj json {:keywordize? true}))
  ([json {:keys [keywordize? default] :or {keywordize? true default {}}}]
   #?(:clj
      (if-not (str/blank? json)
        (j/read-str json :key-fn (if keywordize? keyword identity))
        default)
      :cljs
      (if-not (str/blank? json)
        (let [clj (js->clj (js/JSON.parse json))]
          (if keywordize?
            (walk/keywordize-keys clj)
            clj))
        default))))
