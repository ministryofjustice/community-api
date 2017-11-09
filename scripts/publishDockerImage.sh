#!/bin/bash

PATH=${1}
APPLICATION_NAME=${2}
BUILD_VERSION=${3}

CIRCLE_BUILD_NUM=${4}
CIRCLE_BUILD_URL=${5}
CIRCLE_SHA1=${6}

DOCKER_EMAIL=${7}
DOCKER_USERNAME=${8}
DOCKER_PASSWORD=${9}

cd $PWD

IMAGE_TAG="mojdigitalstudio/$APPLICATION_NAME:$BUILD_VERSION"

docker build $PATH \
  --tag $IMAGE_TAG \
  --label "maintainer=noms-studio-webops@digital.justice.gov.uk" \
  --label "build.number=$CIRCLE_BUILD_NUM" \
  --label "build.url=$CIRCLE_BUILD_URL" \
  --label "build.gitref=$CIRCLE_SHA1"

docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD

docker push $IMAGE_TAG
