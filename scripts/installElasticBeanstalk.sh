#!/bin/bash

cd $PWD

sudo apt-get install python-pip python-dev
pip install --user 'six>-1.9.0' awsebcli --ignore-installed
