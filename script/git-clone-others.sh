#!/bin/bash

# Git clone the other repositories

initializeWorkingDirAndScriptDir() {
    # Set working directory and remove all symbolic links
    workingDir=`pwd -P`

    # Go the script directory
    cd `dirname $0`
    # If the file itself is a symbolic link (ignoring parent directory links), then follow that link recursively
    # Note that scriptDir=`pwd -P` does not do that and cannot cope with a link directly to the file
    scriptFileBasename=`basename $0`
    while [ -L "$scriptFileBasename" ] ; do
        scriptFileBasename=`readlink $scriptFileBasename` # Follow the link
        cd `dirname $scriptFileBasename`
        scriptFileBasename=`basename $scriptFileBasename`
    done
    # Set script directory and remove other symbolic links (parent directory links)
    scriptDir=`pwd -P`
}
initializeWorkingDirAndScriptDir

startDateTime=`date +%s`

gitUrlPrefix="git@github.com:droolsjbpm/"
# TODO dynamic gitUrlPrefix detection does not work on mac
# cd $scriptDir
# gitUrlPrefix=`git remote -v | grep --regex "^origin.*(fetch)$"`
# gitUrlPrefix=`echo $gitUrlPrefix | sed 's/^origin\s*//g' | sed 's/droolsjbpm\-build\-bootstrap\.git\s*(fetch)$//g'`

droolsjbpmOrganizationDir="$scriptDir/../.."
cd $droolsjbpmOrganizationDir

for repository in `cat ${scriptDir}/repository-list.txt` ; do
    echo
    if [ -d $repository ] ; then
        echo "==============================================================================="
        echo "This directory already exists: $repository"
        echo "==============================================================================="
    else
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="
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
