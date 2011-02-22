#!/bin/sh

# Runs a git command on all droolsjbpm repositories.

scriptDir="$( cd "$( dirname "$0" )" && pwd )"
echo ScriptDir: $scriptDir

if [ $# = 0 ] ; then
    echo
    echo "Usage:"
    echo "  $0 [arguments of git]"
    echo "For example:"
    echo "  $0 fetch"
    echo "  $0 pull --rebase"
    echo "  $0 commit -m\"JIRAKEY-1 Fix typo\""
    echo
    exit 1
fi

startDateTime=`date +%s`

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
    git $*
    gitReturnCode=$?
    cd ..
    if [ $gitReturnCode != 0 ] ; then
        exit $?
    fi
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
echo
