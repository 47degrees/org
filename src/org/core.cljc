(ns org.core
  (:require
   [rum.core :as rum]
   [clojure.set :as set]))

(defn sum-by
  [f coll]
  (transduce (map f) + coll))

(defn count-languages
  [repos]
  (count (transduce
          (map :languages)
          clojure.set/union
          repos)))


(rum/defcs navigation < (rum/local false :visible?)
  [{:keys [visible?]}]
  (let [is-visible? @visible?
        toggle-visibility (fn [ev]
                            (.preventDefault ev)
                            (swap! visible? not))]
    (let [menu (if is-visible?
                 :div.menu.is-visible
                 :div.menu)
          fade (if is-visible?
                 :div.menu-panel-fade-screen.is-visible
                 :div.menu-panel-fade-screen)]
      [:nav
       [:div.brand [:img {:src "img/nav-brand.png", :alt ""}]]
       [:div.panel-button
        [:span.octicon.octicon-three-bars.menu-panel-button {:on-click toggle-visibility}]]
       [menu
        [:ul
         [:li [:a {:href "#"} "How to"]]
         [:li [:a {:href "#"} "Blog"]]
         [:li [:a {:href "#"} "Contact"]]]]
       [fade {:on-click toggle-visibility}]])))

(rum/defc stats
  [repos]
  [:div.github-stats
   [:ul
    [:li.contributors
     [:span (sum-by :contributors repos)]
     [:span [:span.octicon.octicon-person] "contributors"]]
    [:li.stars
     [:span (sum-by :stars repos)]
     [:span [:span.octicon.octicon-star] "stars"]]
    [:li.repositories
     [:span (count repos)]
     [:span [:span.octicon.octicon-repo] "repositories"]]
    [:li.languages
     [:span (count-languages repos)]
     [:span [:span.octicon.octicon-code] "languages"]]]])

(rum/defc header
  [organization repos]
  [:header#site-header
   [:div.wrapper
    (navigation)
    [:h1 (str "Open Source Projects by " organization)]
    (stats repos)]])

(rum/defc app < rum/reactive
  [state]
  (let [{:keys [organization repos]} (rum/react state)]
    (header organization repos)))
