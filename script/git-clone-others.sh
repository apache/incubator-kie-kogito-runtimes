#!/bin/sh

# Git clone the other repositories

scriptDir=$(dirname $(readlink -e $0))

startDateTime=`date +%s`

gitUrlPrefix="git@github.com:droolsjbpm/"
# TODO dynamic gitUrlPrefix detection does not work on mac
# cd $scriptDir
# gitUrlPrefix=`git remote -v | grep --regex "^origin.*(fetch)$"`
# gitUrlPrefix=`echo $gitUrlPrefix | sed 's/^origin\s*//g' | sed 's/droolsjbpm\-build\-bootstrap\.git\s*(fetch)$//g'`

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
