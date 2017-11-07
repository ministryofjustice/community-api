#!/bin/bash

BUILD_VERSION=${1}

cd $PWD

./scripts/generateDockerrun.sh ${BUILD_VERSION}

~/.local/bin/eb deploy --process --verbose
