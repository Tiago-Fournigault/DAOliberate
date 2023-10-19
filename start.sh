#!/bin/bash

cd /app/cert
./gen.sh

cd /app/server
mvn exec:java > /dev/null &

cd /app/register
mvn exec:java > /dev/null &
