(ns org.state)

(def configuration
  {:organization "47deg"
   :languages #{"Scala" "Clojure" "Java" "Swift"}
   :token "0ea220b5c8de1be060c132e24771ed74537821be"
   :style {:primary-color "#F44336"
         :font {:url "https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Poppins:300,400"
                :base "'Open sans', sans-serif"
                :headings "'Poppins', sans-serif"
                :sizes {:light 300
                        :regular 400
                        :semi-bold 600
                        :bold 700}}}})

(def default-state
  {:filter-language nil
   :order :stars
   :query ""})
