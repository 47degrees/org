#!/bin/sh

# Configure git
git config --global user.name "Travis CI"
git config --global user.email "al.gg@47deg.com"

# Commit built site
git checkout master
git add docs
git commit -m "[skip ci] Generate site"
git push https://47deg:$GITHUB_API_KEY@github.com/47deg/org master

# Push built subtree to official website
git subtree push --prefix=docs https://47deg:$GITHUB_API_KEY@github.com/47deg/47deg.github.io master 1> /dev/null
