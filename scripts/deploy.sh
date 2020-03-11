#!/bin/sh

# Configure git

git config --global user.name $USER_NAME
git config --global user.email $USER_EMAIL

# Build & Commit built site
git checkout master
lein run
git add docs
git commit -m "[skip ci] Generate site"
git push https://47degrees:$GITHUB_API_KEY@github.com/47degrees/org master
