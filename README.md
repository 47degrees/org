# org

Easily create a webpage with your organization's open source projects

## Overview

`org` is a Clojure(Script) project that allows you to quickly bootstrap a website with your organization's open source projects.
It generates a prerendered static website with all the repository information, as well as fetching the latest data and updating
itself when the user visits the page.

## Creating your own site

First and foremost, ensure you have [Leiningen](http://leiningen.org) and [Sass](http://sass-lang.com) installed.

### Clone the repository

Clone this repository to a local directory (`my-org` in our example):

```sh
$ git clone https://github.com/47deg/org.git my-org
$ cd my-org
```

You'll want to configure the repo to point at your organization repo:

```sh
$ git remote rm origin
$ git remote add origin https://github.com/my-org/my-org.github.io.git
```

Since organization pages must be the content of the `master` branch, you may want to create another
branch for hosting the "raw" website:

```sh
$ git checkout -b raw
$ git push -u origin raw
```

### Edit the configuration

For creating your own site you simple have to provide a configuration file under `resources/config.edn`. The file uses [edn syntax](https://github.com/edn-format/edn)
and its pretty straightforward, here is an example with the defaults (see `resources/config.edn.example`):

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
 :analytics "a-google-analytics-token"
 :footer {:acknowledgment true}
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
- `:logo` is a map with the source URL (`:src`) of your org logo and where it should link to (`:href`), optionally with 
   the styles for the `img` tag (`:style`)
- `:links` is a vector with maps that containt link text (`:text`) and href (`:href`)
- `:social` is a map for specifying the handles in different social networks
 + `:twitter` contains your organization's Twitter handle (without the @)
 + `:facebook` contains your organization's Facebook handle
 + `:linkedin` contains your LinkedIn handle
- `:languages` is the set with the languages you are interested in filtering by
- `:included-projects` is the set with the projects you are interested in having on the site
- `:extra-repos` allows you to add extra repos out of the org (maps with `:user` and `:repo`)
- `:project-logos` is a map from project names to the URL where their logo can be found, libraries without logos will show a placeholder
- `:token` is a string containing the GitHub token required to use the GitHub API
- `:style` is a map with style configuration
 + `:primary-color` sets the primary color of the webpage
 + `:header` controls the styles of the header, only `:background` is supported for now
 + `:font` configures different CSS font settings such as the URL and the heading or base typographies
- `:footer` is for settings related to the footer
 + `:acknowledgment` is a boolean flag for toggling the "by 47 Degrees" acknowledgment in the footer
- `:analytics` is an optional string containing a Google analytics token
 
### Preview your site

After editing the configuration, you can preview the site by running

```sh
$ lein figwheel
```

and pointing your browser to [localhost:3449](http://localhost:3449).

### Build your site

You can create the static site under the `docs` directory by running:

```sh
$ lein run
```

After that, you may want to publish the `docs` directory to a branch so its served by GitHub.
Assuming you want to push the `docs` directory as the content of the `master` branch, you can do:

```sh
git subtree push --prefix docs origin master
```
	
	
Congratulations, you just built your organization's open source project site!

#### SASS compilation

For compiling the SASS styles into CSS, run the following command at the root of the directory:

    sass sass/style.scss:resources/public/style.css

If you want sass to be automatically recompiled when modifying .scss files add the `--watch` flag:

    sass --watch sass/style.scss:resources/public/style.css

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
