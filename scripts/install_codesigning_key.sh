#!/usr/bin/env bash
set -e
set -x

openssl aes-256-cbc -K ${encrypted_676853447788_key:?not set} -iv ${encrypted_676853447788_iv:?not set} -in codesigning.asc.enc -out codesigning.asc -d
gpg --fast-import -q codesigning.asc
