#!/bin/bash

set -euo pipefail

cd server/src/main/java
if ! [ -d .git ];
then
    git init -b main --quiet --object-format=sha256
    git config commit.gpgSign false
    git config tag.gpgSign false
    git remote add upstream DISABLE
    git add --all -f
    git commit --author="Initial <auto@mated.null>" -m "initial" --quiet
    git tag base
fi

git reset --hard base
git am --3way --ignore-whitespace ../../../../patches/*
