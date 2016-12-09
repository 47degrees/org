# org

Easily create a webpage with your organization's open source projects

## Overview

`org` is a Clojure(Script) project that allows you to quickly bootstrap a website with your organization's open source projects.
It generates a prerendered static website with all the repository information, as well as fetching the latest data and updating
itself when the user visits the page.

## Creating your own site

First and foremost, ensure you have [Leiningen](http://leiningen.org) and [Sass](http://sass-lang.com) installed.

For creating your own site you simple have to provide a configuration file under `resources/public/config.edn`. The file uses [edn syntax](https://github.com/edn-format/edn) and its pretty straightforward, here is an example with the defaults:

```clojure
{:organization "47deg"
 :logo {:src "img/logo.png"
        :href "http://47deg.com"}
 :links [{:text "Blog" :href "http://47deg.com/blog"}
         {:text "Contact" :href "mailto:hello@47deg.com"}]
 :social {:twitter "47deg"
          :facebook "47degreesLLC"}
 :languages #{"Scala" "Clojure" "Java" "Swift"}
 :included-projects #{"macroid"
                      "fetch"
                      "sbt-microsites"
                      "scalacheck-datetime"
                      "github4s"
                      "org"
                      "sbt-catalysts-extras"
                      "sbot"
                      "freestyle"
                      "mvessel"
                      "case-classy"
                      "second-bridge"}
 :project-logos {"fetch" "https://rawgit.com/47deg/microsites/cdn//fetch/navbar_brand.png"
                 "mvessel" "https://rawgit.com/47deg/microsites/cdn//mvessel/navbar_brand.png"
                 "github4s" "https://rawgit.com/47deg/microsites/cdn//github4s/navbar_brand.png"
                 "scalacheck-datetime" "https://rawgit.com/47deg/microsites/cdn//scalacheck-datetime/navbar_brand.png"}
 :token "a-github-token"
 :style {:primary-color "#F44336"
         :header {:background "linear-gradient(0deg, rgba(30, 39, 53, 0.88), rgba(30, 39, 53, 0.88)), url(\"../img/header-background.jpg\") no-repeat center center"}
         :font {:url "https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Poppins:300,400"
                :base "'Open sans', sans-serif"
                :headings "'Poppins', sans-serif"
                :sizes {:light 300
                        :regular 400
                        :semi-bold 600
                        :bold 700}}}}
```

Let's break it down:

- `:organization` is the name of your org in GitHub
- `:organization-name` is the human-readable name of your org. `:organization` will be used if not provided.
- `:logo` is a map with the source URL (`:src`) of your org logo and where it should link to (`:href`)
- `:links` is a vector with maps that containt link text (`:text`) and href (`:href`)
- `:social` is a map for specifying the handles in different social networks
 + `:twitter` contains your organization's Twitter handle (without the @)
 + `:facebook` contains your organization's Facebook handle
- `:languages` is the set with the languages you are interested in filtering by
- `:included-projects` is the set with the projects you are interested in having on the site
- `:project-logos` is a map from project names to the URL where their logo can be found, libraries without logos will show a placeholder
- `:token` is a string containing the GitHub token required to use the GitHub API
- `:style` is a map with style configuration
 + `:primary-color` sets the primary color of the webpage
 + `:header` controls the styles of the header, only `:background` is supported for now
 + `:font` configures different CSS font settings such as the URL and the heading or base typographies

After you write your config file you can create the site under the `docs` directory by running:

    lein run

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

## SASS compilation

For compiling the SASS styles into CSS, run the following command at the root of the directory:

    sass sass/style.scss:resources/public/style.css

If you want sass to be automatically recompiled when modifying .scss files add the `--watch` flag:

    sass --watch sass/style.scss:resources/public/style.css

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
