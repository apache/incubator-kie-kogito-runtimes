#!/bin/sh

# Runs a mvn command on all droolsjbpm repositories.

scriptDir="$( cd "$( dirname "$0" )" && pwd )"
echo ScriptDir: $scriptDir

if [ $# = 0 ] ; then
    echo
    echo "Usage:"
    echo "  $0 [mvn arguments]"
    echo "For example:"
    echo "  $0 --version"
    echo "  $0 -DskipTests clean install"
    echo "  $0 -Dfull clean install"
    echo
    exit 1
fi

droolsjbpmOrganizationDir="$scriptDir/../.."
cd $droolsjbpmOrganizationDir

repositories="droolsjbpm-build-bootstrap;droolsjbpm-knowledge;drools;drools-planner;droolsjbpm-integration;guvnor;droolsjbpm-tools;droolsjbpm-build-distribution"
repositories="$(echo $repositories | sed 's/;/\n/g')" # convert to array

for repository in $repositories ; do
    echo
    echo "==============================================================================="
    echo "Repository: $repository"
    echo "==============================================================================="
    echo
    cd $repository
    mvn $*
    cd ..
    if [ $? != 0 ] ; then
        exit $?
    fi
done
