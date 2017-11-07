#!/bin/bash

BUILD_VERSION=${1}

cd $PWD

cat <<- _EOF_ > Dockerrun.aws.json
{
  "AWSEBDockerrunVersion": "1",
  "Image": {
    "Name": "mojdigitalstudio/deliusapi:${BUILD_VERSION}",
    "Update": "true"
  },
  "Ports": [
    {
      "hostPort": "80",
      "ContainerPort": "8080"
    }
  ]
}
_EOF_
