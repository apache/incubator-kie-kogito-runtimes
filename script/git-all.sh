#!/bin/bash

# Runs a git command on all droolsjbpm repositories.

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
droolsjbpmOrganizationDir="$scriptDir/../.."

# these variables don't make any sense right now
#
#withoutJbpm="$withoutJbpm"
#withoutUberfire="$withoutUberfire"


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

cd "$droolsjbpmOrganizationDir"

for repository in `cat "${scriptDir}/repository-list.txt"` ; do
    echo
    if [ ! -d "$droolsjbpmOrganizationDir/$repository" ]; then
        echo "==============================================================================="
        echo "Missing Repository: $repository. SKIPPING!"
        echo "==============================================================================="
    else
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="
        cd $repository

        git "$@"

        returnCode=$?
        cd ..
        if [ $returnCode != 0 ] ; then
            echo -n "Error executing command for repository ${repository}. Should I continue? (Hit control-c to stop or enter to continue): "
            read ok
        fi
    fi
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
