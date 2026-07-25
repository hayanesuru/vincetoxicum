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
    exit 0
fi

if [ -d "../../../../patches" ]; then rm -r ../../../../patches; fi
git format-patch -N --no-signature --zero-commit --no-stat --full-index --minimal --output-directory=../../../../patches -m "$(git show-ref -s base)"
