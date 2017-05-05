#!/bin/sh
# Utility script for removing ignores in revapi-config.json files.

usage ()
{
  echo "################################## Revapi clean ##################################"
  echo "Usage : `basename $0` <latestFinalCommunityVersion> <futureFinalCommunityVersion>"
  echo "        for example: ./revapi-clean.sh 7.0.0.Final 7.1.0.Final"
  echo "        OR"
  echo "        `basename $0` -h for help"
  echo "##################################################################################"
  exit
}

getopts :h opt

if [ "$opt" == "h" ]; then
    usage
elif [ "$#" -ne 2 ]; then
    usage
else
    # First, delete ignores
    find . -iname "revapi-config.json" -exec perl -i -0pe 's/("ignore":.*)\[.*\]/\1\[\]/s' {} \;
    # Secondly, change versions in the comment
    find . -iname "revapi-config.json" -exec perl -i -0pe "s/(\"Changes between).*(and the current branch.*when).*(is available)/\1 $1 \2 $2 \3/s" {} \;
fi
