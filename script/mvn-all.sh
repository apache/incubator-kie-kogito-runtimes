#!/bin/bash

# Runs a mvn command on all droolsjbpm repositories.

realpath() {

    TARGET_FILE=$1

    cd `dirname $TARGET_FILE`
    TARGET_FILE=`basename $TARGET_FILE`

    # Iterate down a (possible) chain of symlinks
    while [ -L "$TARGET_FILE" ]
    do
        TARGET_FILE=`readlink $TARGET_FILE`
        cd `dirname $TARGET_FILE`
        TARGET_FILE=`basename $TARGET_FILE`
    done


    # Compute the canonicalized name by finding the physical path 
    # for the directory we're in and appending the target file.
    PHYS_DIR=`pwd -P`
    scriptDir=$PHYS_DIR
    echo $RESULT
}

realpath $0

if [ $# = 0 ] ; then
    echo
    echo "Usage:"
    echo "  $0 [arguments of mvn]"
    echo "For example:"
    echo "  $0 --version"
    echo "  $0 -DskipTests clean install"
    echo "  $0 -Dfull clean install"
    echo
    exit 1
fi

startDateTime=`date +%s`

droolsjbpmOrganizationDir="$scriptDir/../.."
cd $droolsjbpmOrganizationDir

for repository in `cat ${scriptDir}/repository-list.txt` ; do
    echo
    if [ -d $droolsjbpmOrganizationDir/$repository ] ; then
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="
        cd $repository
        mvn $*
        mvnReturnCode=$?
        cd ..
        if [ $mvnReturnCode != 0 ] ; then
            exit $mvnReturnCode
        fi
    else
        echo "==============================================================================="
        echo "Missing Repository: $repository. Skipping"
        echo "==============================================================================="
    fi
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
