#!/bin/sh

# Runs a git command on all droolsjbpm repositories.

scriptDir=$(readlink -f  $(dirname $0))

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

droolsjbpmOrganizationDir="$scriptDir/../.."
cd $droolsjbpmOrganizationDir

for repository in `cat ${scriptDir}/repository-list.txt` ; do
    if [ -d $droolsjbpmOrganizationDir/$repository ] ; then
        echo -e "\n==============================================================================="
        echo "Repository: $repository"
        echo -e "===============================================================================\n"
        cd $repository
        git $*
        gitReturnCode=$?
        cd ..
        if [ $gitReturnCode != 0 ] ; then
            exit $gitReturnCode
        fi
    else
        echo -e "\n==============================================================================="
        echo "Missing Repository: $repository. Skipping"
        echo -e "===============================================================================\n" 
    fi
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
echo
