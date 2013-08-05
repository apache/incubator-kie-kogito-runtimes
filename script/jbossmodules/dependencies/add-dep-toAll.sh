#!/bin/bash

if [ $# -ne 1 ];
then
  echo "Missing arguments"
  exit 65
fi

for i in *.dependencies
do 
   echo "$1" >> $i
done

