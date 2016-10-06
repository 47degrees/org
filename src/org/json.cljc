(ns org.json
  #?(:clj
     (:require [clojure.data.json :as j])
     :cljs
     (:require [clojure.walk :as walk])))

(defn json->clj
  ([json]
   (json->clj json {:keywordize? true}))
  ([json {:keys [keywordize?]}]
   #?(:clj
      (j/read-str json :key-fn (if keywordize? keyword identity))
      :cljs
      (let [clj (js->clj (js/JSON.parse json))]
        (if keywordize?
          (walk/keywordize-keys clj)
          clj)))))
