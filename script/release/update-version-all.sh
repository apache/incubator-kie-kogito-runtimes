#!/bin/bash

# Update the version for for all droolsjbpm repositories

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

if [ $# != 2 ] && [ $# != 4 ] && [ $# != 6 ] ; then
    echo
    echo "Usage:"
    echo "  $0 droolsOldVersion droolsNewVersion [jbpmOldVersion jbpmNewVersion] [uberfireOldVersion uberfireNewVersion]"
    echo "For example:"
    echo "  $0 5.2.0-SNAPSHOT 5.2.0.Final 5.1.0-SNAPSHOT 5.1.0.Final 0.2.0-SNAPSHOT 0.2.0.Final"
    echo
    exit 1
fi
droolsOldVersion=$1
droolsNewVersion=$2
echo "The drools, guvnor, ... version: old is $droolsOldVersion - new is $droolsNewVersion"
if [ "$withoutJbpm" != 'true' ]; then
    jbpmOldVersion=$3
    jbpmNewVersion=$4
    echo "The jbpm version: old is $jbpmOldVersion - new is $jbpmNewVersion"
fi
if [ "$withoutUberfire" != 'true' ]; then
    uberfireOldVersion=$5
    uberfireNewVersion=$6
    echo "The Uberfire version: old is $uberfireOldVersion - new is $uberfireNewVersion"
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

        # WARNING: Requires a fix for http://jira.codehaus.org/browse/MRELEASE-699 to work!
        # ge0ffrey has 2.2.2-SNAPSHOT build locally, patched with MRELEASE-699
        if [ $repository != 'droolsjbpm-tools' ]; then
            if [ $repository == 'droolsjbpm-build-bootstrap' ]; then
                mvn -Dfull versions:set -DoldVersion=$droolsOldVersion -DnewVersion=$droolsNewVersion -DallowSnapshots=true -DgenerateBackupPoms=false
                # TODO remove this WORKAROUND for http://jira.codehaus.org/browse/MVERSIONS-161
                mvn clean install -DskipTests
            elif [ $repository == 'jbpm' ]; then
                mvn -Dfull versions:set -DoldVersion=$jbpmOldVersion -DnewVersion=$jbpmNewVersion -DallowSnapshots=true -DgenerateBackupPoms=false
                mvn -Dfull versions:update-parent -DparentVersion=[$droolsNewVersion] -DallowSnapshots=true -DgenerateBackupPoms=false
                mvn -Dfull versions:update-child-modules -DallowSnapshots=true -DgenerateBackupPoms=false
            elif [ $repository == 'jbpm-console-ng' ]; then
                mvn -Dfull versions:set -DoldVersion=$jbpmOldVersion -DnewVersion=$jbpmNewVersion -DallowSnapshots=true -DgenerateBackupPoms=false
                mvn -Dfull versions:update-parent -DparentVersion=[$droolsNewVersion] -DallowSnapshots=true -DgenerateBackupPoms=false
                mvn -Dfull versions:update-child-modules -DallowSnapshots=true -DgenerateBackupPoms=false
            elif [ $repository == 'uberfire' ]; then
                mvn -Dfull versions:set -DoldVersion=$uberfireOldVersion -DnewVersion=$uberfireNewVersion -DallowSnapshots=true -DgenerateBackupPoms=false
                # TODO remove this WORKAROUND for http://jira.codehaus.org/browse/MVERSIONS-161
                mvn clean install -DskipTests
            else
                mvn -Dfull versions:update-parent -DparentVersion=[$droolsNewVersion] -DallowSnapshots=true -DgenerateBackupPoms=false
                mvn -Dfull versions:update-child-modules -DallowSnapshots=true -DgenerateBackupPoms=false
            fi
            returnCode=$?
        else
            cd drools-eclipse
            mvn -Dfull tycho-versions:set-version -DnewVersion=$droolsNewVersion
            returnCode=$?
            cd ..
            if [ $returnCode == 0 ]; then
                mvn -Dfull versions:update-parent -N -DparentVersion=[$droolsNewVersion] -DallowSnapshots=true -DgenerateBackupPoms=false
                # TODO remove this WORKAROUND for http://jira.codehaus.org/browse/MVERSIONS-161
                mvn clean install -N -DskipTests
                cd drools-eclipse
                mvn -Dfull versions:update-parent -N -DparentVersion=[$droolsNewVersion] -DallowSnapshots=true -DgenerateBackupPoms=false
                cd ..
                mvn -Dfull versions:update-child-modules -DallowSnapshots=true -DgenerateBackupPoms=false
                returnCode=$?
            fi
            # TODO drools-ant, drools-eclipse, droolsjbpm-tools-distribution
        fi

        cd ..
        if [ $returnCode != 0 ] ; then
            exit $returnCode
        fi
    fi
done

cd droolsjbpm-build-distribution
mvn antrun:run -N -DdroolsOldVersion=$droolsOldVersion -DdroolsNewVersion=$droolsNewVersion -DjbpmOldVersion=$jbpmOldVersion -DjbpmNewVersion=$jbpmNewVersion -DuberfireOldVersion=$uberfireOldVersion -DuberfireNewVersion=$uberfireNewVersion
returnCode=$?
cd ..
if [ $returnCode != 0 ] ; then
    exit $returnCode
fi

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
