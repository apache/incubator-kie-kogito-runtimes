#!/bin/sh

# Git clone the other repositories

scriptDir="$( cd "$( dirname "$0" )" && pwd )"
echo ScriptDir: $scriptDir

startDateTime=`date +%s`

cd $scriptDir
gitUrlPrefix=`git remote -v | grep --regex "^origin.*(fetch)$"`
gitUrlPrefix=`echo $gitUrlPrefix | sed 's/^origin\s*//g' | sed 's/droolsjbpm\-build\-bootstrap\.git\s*(fetch)$//g'`
echo "gitUrl: $gitUrlPrefix"

droolsjbpmOrganizationDir="$scriptDir/../.."
cd $droolsjbpmOrganizationDir

repositories="droolsjbpm-knowledge;drools;drools-planner;droolsjbpm-integration;guvnor;droolsjbpm-tools;droolsjbpm-build-distribution"
repositories="$(echo $repositories | sed 's/;/\n/g')" # convert to array

for repository in $repositories ; do
    if [ -d $repository ] ; then
        echo "This directory already exists: $repository"
    else
        echo "Cloning: $repository"
        git clone ${gitUrlPrefix}${repository}.git ${repository}
        if [ $? != 0 ] ; then
            exit $?
        fi
    fi
done

for repository in $repositories ; do
    du -sh $repository
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
echo
