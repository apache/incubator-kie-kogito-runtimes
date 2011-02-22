#!/bin/sh

# Git clone the other repositories

scriptDir="$( cd "$( dirname "$0" )" && pwd )"

startDateTime=`date +%s`

cd $scriptDir
gitUrlPrefix=`git remote -v | grep --regex "^origin.*(fetch)$"`
gitUrlPrefix=`echo $gitUrlPrefix | sed 's/^origin\s*//g' | sed 's/droolsjbpm\-build\-bootstrap\.git\s*(fetch)$//g'`
echo "gitUrl: $gitUrlPrefix"

droolsjbpmOrganizationDir="$scriptDir/../.."
cd $droolsjbpmOrganizationDir

for repository in `cat ${scriptDir}/repository-list.txt` ; do
    if [ -d $repository ] ; then
        echo "This directory already exists: $repository"
    else
        echo
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="
        echo
        git clone ${gitUrlPrefix}${repository}.git ${repository}
        if [ $? != 0 ] ; then
            exit $?
        fi
    fi
done

echo
echo Disk size:

for repository in `cat ${scriptDir}/repository-list.txt` ; do
    du -sh $repository
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
echo
