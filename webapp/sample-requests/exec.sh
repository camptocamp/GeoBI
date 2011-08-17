#!/bin/sh

if [ -z "$1" ]; then
  echo "The file containing the request is required"
  exit 1
fi

curl -d @$1 -H "Content-Type:text/xml" http://uabeea-geobi-dev.int.lsn.camptocamp.com/geomondrian/xmla