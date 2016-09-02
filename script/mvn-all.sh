#!/bin/bash

# Run a mvn command on all droolsjbpm repositories.

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

# default repository list is stored in the repository-list.txt file
REPOSITORY_LIST=`cat "${scriptDir}/repository-list.txt"`
MVN_ARG_LINE=""

for arg in "$@"
do
    case "$arg" in
        --repo-list=*)
            REPOSITORY_LIST=$(echo $arg | sed 's/[-a-zA-Z0-9]*=//')
            # replace the commas with spaces so that the for loop treats the individual repos as different values
            REPOSITORY_LIST=${REPOSITORY_LIST//,/ }
        ;;

        *)
            MVN_ARG_LINE="$MVN_ARG_LINE$arg"
        ;;
    esac
done

if [ "x$MVN_ARG_LINE" = "x" ] ; then
    echo
    echo "Usage:"
    echo "  $0 <arguments of maven> [--repo-list=<list-of-repositories>]"
    echo "For example:"
    echo "  $0 --version"
    echo "  $0 -DskipTests clean install"
    echo "  $0 -Dfull clean install"
    echo "  $0 clean test --repo-list=drools,jbpm"
    echo
    exit 1
fi

startDateTime=`date +%s`

cd "$droolsjbpmOrganizationDir"

for repository in $REPOSITORY_LIST; do
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

        if [ -a $M3_HOME/bin/mvn ] ; then
            $M3_HOME/bin/mvn $MVN_ARG_LINE
        else
            mvn $MVN_ARG_LINE
        fi

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
