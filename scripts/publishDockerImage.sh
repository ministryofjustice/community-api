#!/bin/bash

APPLICATION_NAME=${1}
BUILD_VERSION=${2}

CIRCLE_BUILD_NUM=${3}
CIRCLE_BUILD_URL=${4}
CIRCLE_SHA1=${5}

DOCKER_EMAIL=${6}
DOCKER_USERNAME=${7}
DOCKER_PASSWORD=${8}

cd $PWD

IMAGE_TAG="mojdigitalstudio/$APPLICATION_NAME:$BUILD_VERSION"

docker build . \
  --tag $IMAGE_TAG \
  --label "maintainer=noms-studio-webops@digital.justice.gov.uk" \
  --label "build.number=$CIRCLE_BUILD_NUM" \
  --label "build.url=$CIRCLE_BUILD_URL" \
  --label "build.gitref=$CIRCLE_SHA1"

docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD

docker push $IMAGE_TAG
