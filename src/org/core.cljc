(ns org.core
  (:require
   [rum.core :as rum]
   [cuerdas.core :as str]
   [clojure.set :as set]))


(defn repos-by-config
  [repos {:keys [included-projects archived-projects]}]
  (sequence (comp
              (remove #(contains? archived-projects (:name %)))
              (filter #(contains? included-projects (:name %))))
            repos))

(defn parens
  [x]
  (str "(" x ")"))

(defn sum-by
  [f coll]
  (transduce (map f) + coll))

(defn all-languages
  [repos]
  (transduce
   (map :languages)
   clojure.set/union
   repos))

(defn filter-by-language
  [lang repos]
  (filter (fn [repo]
           (contains? (:languages repo) lang))
          repos))

(defn languages-by-count
  [repos]
  (let [langs (into [] (all-languages repos))]
    (reverse (sort-by #(count (filter-by-language % repos)) langs))))

(defn sum-contributors
  [repos]
  (count
    (into #{} (mapcat :contributors) repos)))

(rum/defc link-list
  [links]
  [:ul
   (for [{:keys [text href]} links]
     [:li
      {:key text}
      [:a {:href href} text]])])

(rum/defcs navigation < (rum/local false :visible?)
  [{:keys [visible?]} {:keys [logo links]}]
  (let [{:keys [src href style]} logo
        is-visible? @visible?
        toggle-visibility (fn [ev]
                            (.preventDefault ev)
                            (swap! visible? not))
        menu (if is-visible?
                 :div.menu.is-visible
                 :div.menu)
        fade (if is-visible?
               :div.menu-panel-fade-screen.is-visible
               :div.menu-panel-fade-screen)]
    [:nav
     [:div.brand
      [:a {:href href}
       [:img {:src src :style style}]]]
     [:div.panel-button
      [:span.octicon.octicon-three-bars.menu-panel-button {:on-click toggle-visibility}]]
     [menu
      (link-list links)]
     [fade {:on-click toggle-visibility}]]))

(rum/defc stats
  [repos]
  [:div.github-stats
   [:ul
    [:li.contributors
     [:span (sum-contributors repos)]
     [:span [:span.octicon.octicon-person] "contributors"]]
    [:li.stars
     [:span (transduce (map :stars) + repos)]
     [:span [:span.octicon.octicon-star] "stars"]]
    [:li.repositories
     [:span (count repos)]
     [:span [:span.octicon.octicon-repo] "repositories"]]
    [:li.languages
     [:span (count (all-languages repos))]
     [:span [:span.octicon.octicon-code] "languages"]]]])

(rum/defc header
  [{:keys [organization-name organization logo style] :as config} repos]
  (let [org (or organization-name organization)
        headline (str "Open Source Projects by " org)]
    [:header#site-header
     {:style (:header style)}
     [:title headline]
     [:div.wrapper
      (navigation config)
      [:h1 headline]
      (stats repos)]]))

(defn humanize-order
  [order]
  (case order
    :stars "Stars"
    :forks "Forks"
    :updated "Updated"))

(def esc 27)

(def ordering-mixin
  #?(:cljs
     {:did-mount
      (fn [state]
        (let [local-state (:expanded? state)
              order-el (rum/ref state "order")]
          ;; close on esc
          (js/document.addEventListener "keydown" (fn [ev]
                                                    (when (= (.-keyCode ev) esc)
                                                      (reset! local-state false))))
          ;; close on click outside
          (js/document.addEventListener "click" (fn [ev]
                                                  (let [target (.-target ev)]
                                                    (when-not (.contains order-el target)
                                                      (reset! local-state false))))))
        state)}
     :clj
     {}))

(rum/defcs ordering < rum/reactive (rum/local false :expanded?) ordering-mixin
  [{:keys [expanded?]} state]
  (let [is-expanded? (rum/react expanded?)
        {:keys [order]} (rum/react state)
        expand (fn [ev]
                 (.preventDefault ev)
                 (reset! expanded? true))
        change-order (fn [new-order]
                       (fn [ev]
                         (.preventDefault ev)
                         (swap! state assoc :order new-order)
                         (reset! expanded? false)))]
    [:div.order-by
     {:ref "order"}
     [:div.dropdown-select
      [:p.description "Order by"]
      [:p.button
       {:on-click expand}
       (humanize-order order)
       [:i.fa.fa-caret-down]]
      (when is-expanded?
        [:ul
         [:li {:on-click (change-order :stars)} "Stars"]
         [:li {:on-click (change-order :forks)} "Forks"]
         [:li {:on-click (change-order :updated)} "Updated"]])]]))

(defn sort-repos
  [repos order]
  (reverse (sort-by order repos)))

(defn repos-by-language
  [repos language]
  (if language
    (filter #(contains? (:languages %) language) repos)
    repos))

(defn repos-by-query
  [repos query]
  (if (str/blank? query)
    repos
    (filter (fn [repo]
              (let [name (str/trim (str/lower (:name repo)))
                    description (str/trim (str/lower (:description repo)))]
                (or (str/includes? name query)
                    (str/includes? description query))))
            repos)))

(def logo-placeholder "img/image-project-info.png")

(rum/defc repo-card
  [{:keys [name description url stars forks contributors languages]} languages-whitelist project-logos]
  (let [filtered-languages (filter #(contains? languages-whitelist %) languages)]
    [:a.project-info
     {:href url
      :key name}
     (if-let [src (get project-logos name)]
       [:img {:src src}]
       [:img {:src logo-placeholder}])
     [:h2 name]
     [:p  description]
     [:ul
      [:li
       {:key "langs"}
       [:span.octicon.octicon-code]
       [:span (if (seq filtered-languages)
                (apply str (interpose ", " filtered-languages))
                "Unknown")]]
      [:li {:key "langs-fill"}]
      [:li
       {:key "forks"}
       [:span.octicon.octicon-git-branch]
       [:span forks]]
      [:li {:key "forks-fill"}]
      [:li
       {:key "stars"}
       [:span.octicon.octicon-star]
       [:span stars]]
      [:li {:key "stars-fill"}]
      [:li
       {:key "contributors"}
       [:span.octicon.octicon-person]
       [:span (count contributors)]]]]))

(rum/defc search < rum/reactive
  [state]
  (let [{:keys [query]} (rum/react state)]
    [:div.search
     [:input
      {:type "text"
       :placeholder "Search a project"
       :on-change (fn [ev]
                    (swap! state assoc :query (.-value (.-target ev))))}]]))

(rum/defc filter-and-sort < rum/reactive
  [state]
  (let [select-lang (fn [lang]
                      (fn [ev]
                        (.preventDefault ev)
                        (swap! state assoc :filter-language lang)))
        {:keys [filter-language
                repos
                config]} (rum/react state)
        repos (repos-by-config repos config)]
    [:div.filter
     [:div.tag
      [:ul
       [:li
        {:key "all"}
        [(if filter-language
           :a
           :a.active)
         {:href "#"
          :on-click (select-lang nil)}
         "All " [:span (parens (count repos))]]]
       (for [lang (languages-by-count repos)
             :when (contains? (:languages config) lang)]
         [:li
          {:key lang}
          [(if (= lang filter-language)
           :a.active
           :a)
           {:href "#"
            :on-click (select-lang lang)}
           lang
           " "
           [:span (parens (count (filter-by-language lang repos)))]]])]]
     (ordering state)]))

(rum/defc main < rum/reactive
  [state]
  (let [{:keys [repos
                query
                filter-language
                order
                config]} (rum/react state)
        base-repos (repos-by-config repos config)
        sorted-repos (sort-repos base-repos order)
        filtered-repos (repos-by-language sorted-repos filter-language)
        searched-repos (repos-by-query filtered-repos query)]
    [:main#site-main
     [:div.wrapper
      (search state)
      (filter-and-sort state)
      [:div.project-list
       (for [repo searched-repos]
         (repo-card repo (:languages config) (:project-logos config)))]]]))

(defn github-url
  [organization]
  (str "http://github.com/" organization))

(defn twitter-url
  [handle]
  (str "http://twitter.com/" handle))

(defn facebook-url
  [handle]
  (str "http://facebook.com/" handle))

(defn linkedin-url
  [handle]
  (str "http://linkedin.com/" handle))

(rum/defc footer
  [{:keys [organization organization-name links social footer]}]
  [:footer#site-footer
   [:div.wrapper
    [:div.navigation
     (link-list links)
     (let [year 2020
           org (or organization-name organization)]
       [:p
        (str "Copyright ©" year " " org " - Built with ")
        [:a {:href "http://github.com/47degrees/org"} "org"]
        (when (get footer :acknowledgment true)
          [:span
           " by "
           [:a {:href "http://47deg.com"} "47 Degrees"]])])]
    [:div.social
     [:ul
      [:li "Follow us"]
      [:li
       [:a {:href (github-url organization)}
        [:i.fa.fa-github]]]
      (when-let [twitter (:twitter social)]
        [:li
         [:a {:href (twitter-url twitter)}
          [:i.fa.fa-twitter]]])
      (when-let [fb (:facebook social)]
        [:li
         [:a {:href (facebook-url fb)}
          [:i.fa.fa-facebook]]])
      (when-let [linkedin (:linkedin social)]
        [:li
         [:a {:href (linkedin-url linkedin)}
          [:i.fa.fa-linkedin]]])]]]])

(rum/defc app < rum/reactive
  [state]
  (let [{:keys [config repos]} (rum/react state)]
    [:div
     (header config repos)
     (main state)
     (footer config)]))
