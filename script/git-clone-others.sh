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
droolsjbpmOrganizationDir="$scriptDir/../.."
withoutJbpm="$withoutJbpm"
withoutUberfire="$withoutUberfire"

startDateTime=`date +%s`

# The gitUrlPrefix differs between committers and anonymous users. Also it differs on forks.
# Committers on blessed gitUrlPrefix="git@github.com:droolsjbpm/"
# Anonymous users on blessed gitUrlPrefix="git://github.com/droolsjbpm/"
cd "${scriptDir}"
gitUrlPrefix=`git remote -v | grep --regex "^origin.*(fetch)$"`
gitUrlPrefix=`echo ${gitUrlPrefix} | sed 's/^origin\s*//g' | sed 's/droolsjbpm\-build\-bootstrap\.git.*//g'`

cd "$droolsjbpmOrganizationDir"

for repository in `cat "${scriptDir}/repository-list.txt"` ; do
    echo
    if [ -d $repository ] ; then
        echo "==============================================================================="
        echo "This directory already exists: $repository"
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
# hack for fuse-bxms-integ repository on github.com:jboss-integration
    elif [ "${repository}" == "fuse-bxms-integ" ]; then
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="
        echo -- prefix git@github.com:jboss-integration/ --
        gitUrlPrefix=git@github.com:jboss-integration/
        echo -- repository ${repository} --
        echo -- ${gitUrlPrefix}${repository}.git -- ${repository} --
        git clone ${gitUrlPrefix}${repository}.git ${repository}       
    else
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="

        echo -- prefix ${gitUrlPrefix} --
        echo -- repository ${repository} --
        echo -- ${gitUrlPrefix}${repository}.git -- ${repository} --
        git clone ${gitUrlPrefix}${repository}.git ${repository}
        
        returnCode=$?
        if [ $returnCode != 0 ] ; then
            exit $returnCode
        fi
    fi
done

echo
echo Disk size:

for repository in `cat "${scriptDir}/repository-list.txt"` ; do
    du -sh $repository
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
