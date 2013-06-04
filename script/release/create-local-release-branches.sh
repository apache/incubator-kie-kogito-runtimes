#!/bin/bash

# Create a release branch for all git repositories

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
droolsjbpmOrganizationDir="$scriptDir/../../.."
withoutJbpm="$withoutJbpm"
withoutUberfire="$withoutUberfire"

if [ $# != 1 ] && [ $# != 2 ] && [ $# != 3 ] ; then
    echo
    echo "Usage:"
    echo "  $0 droolsReleaseBranchName [jbpmReleaseBranchName] [uberfireReleaseBranchName]"
    echo "For example:"
    echo "  $0 5.2.x 5.1.x 0.2.x"
    echo
    exit 1
fi

echo "The drools, guvnor, ... release branch name is $1"
if [ "$withoutJbpm" != 'true' ]; then
    echo "The jbpm release branch name is $2"
fi
if [ "$withoutUberfire" != 'true' ]; then
    echo "The Uberfire release branch name is $3"
fi
echo -n "Is this ok? (Hit control-c if is not): "
read ok

startDateTime=`date +%s`

cd $droolsjbpmOrganizationDir

for repository in `cat ${scriptDir}/../repository-list.txt` ; do
    echo
    if [ ! -d $droolsjbpmOrganizationDir/$repository ]; then
        echo "==============================================================================="
        echo "Missing Repository: $repository. SKIPPING!"
        echo "==============================================================================="
    elif [ "${repository}" != "${repository#jbpm}" ] && [ "$withoutJbpm" = 'true' ]; then
        echo "==============================================================================="
        echo "Without repository: $repository. SKIPPING!"
        echo "==============================================================================="
    elif [ "${repository}" != "${repository#jbpm-console-ng}" ] && [ "$withoutJbpm" = 'true' ]; then
        echo "==============================================================================="
        echo "Without repository: $repository. SKIPPING!"
        echo "==============================================================================="
    elif [ "${repository}" != "${repository#uberfire}" ] && [ "$withoutUberfire" = 'true' ]; then
        echo "==============================================================================="
        echo "Without repository: $repository. SKIPPING!"
        echo "==============================================================================="
    else
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="
        cd $repository

        releaseBranchName=$1
        if [ "${repository}" != "${repository#jbpm}" ]; then
            releaseBranchName=$2
        elif [ "${repository}" != "${repository#jbpm-console-ng}" ]; then
            releaseBranchName=$2
        elif [ "${repository}" != "${repository#uberfire}" ]; then
            releaseBranchName=$3
        fi
        git checkout -b $releaseBranchName

        returnCode=$?
        cd ..
        if [ $returnCode != 0 ] ; then
            exit $returnCode
        fi
    fi
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
echo "Warning: your working branches are now those release branches, NOT master."
