#!/bin/sh

# Runs a mvn command on all droolsjbpm repositories.

scriptDir=$(dirname $(readlink -e $0))

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
    echo "==============================================================================="
    echo "Repository: $repository"
    echo "==============================================================================="
    echo
    cd $repository
    mvn $*
    mvnReturnCode=$?
    cd ..
    if [ $mvnReturnCode != 0 ] ; then
        exit $?
    fi
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
echo
