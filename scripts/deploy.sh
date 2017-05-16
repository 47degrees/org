#!/bin/sh

# Configure git

git config --global user.name "Travis CI"
git config --global user.email "ana@47deg.com"

# Pull latest version of published subtree
git checkout master
git subtree pull --prefix=docs --message="[skip ci] Update subtree" https://47deg:$GITHUB_API_KEY@github.com/47deg/47deg.github.io.git master

# Build & Commit built site
lein run
git add docs
git commit -m "[skip ci] Generate site"
git push https://47deg:$GITHUB_API_KEY@github.com/47deg/org master

# Push built subtree to official website
git subtree push --prefix=docs https://47deg:$GITHUB_API_KEY@github.com/47deg/47deg.github.io.git master
