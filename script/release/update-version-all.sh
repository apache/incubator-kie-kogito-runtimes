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

if [ $# != 2 ] && [ $# != 4 ] ; then
    echo
    echo "Usage:"
    echo "  $0 droolsOldVersion droolsNewVersion [jbpmOldVersion jbpmNewVersion]"
    echo "For example:"
    echo "  $0 5.2.0.Final 5.1.0.Final"
    echo
    exit 1
fi

echo "The drools, guvnor, ... version: old is $1 - new is $2"
if [ $withoutJbpm != 'true' ]; then
    echo "The jbpm version: old is $3 - new is $4"
fi
echo -n "Is this ok? (Hit control-c if is not): "
read ok

startDateTime=`date +%s`

cd $droolsjbpmOrganizationDir

for repository in `cat ${scriptDir}/../repository-list.txt` ; do
    echo
    if [ ! -d $droolsjbpmOrganizationDir/$repository ]; then
        echo "==============================================================================="
        echo "Missing Repository: $repository. Skipping"
        echo "==============================================================================="
    elif [ $repository = 'jbpm' ] && [ $withoutJbpm = 'true' ]; then
        echo "==============================================================================="
        echo "Without repository: $repository. Skipping"
        echo "==============================================================================="
    else
        echo "==============================================================================="
        echo "Repository: $repository"
        echo "==============================================================================="
        cd $repository

        oldVersion=$1
        newVersion=$2
        if [ $repository = 'jbpm' ]; then
            oldVersion=$3
            newVersion=$4
        fi
        # WARNING: Requires a fix for http://jira.codehaus.org/browse/MRELEASE-699 to work!
        # ge0ffrey has 2.2.2-SNAPSHOT build locally, patched with MRELEASE-699
        mvnCommand="mvn --batch-mode org.apache.maven.plugins:maven-release-plugin:2.2.2-SNAPSHOT:update-versions -DreleaseVersion=$newVersion"
        if [ $repository != 'droolsjbpm-tools' ]; then
            $mvnCommand
            returnCode=$?
        else
            $mvnCommand -N
            returnCode=$?
            if [ $returnCode != 0 ]; then
                cd ..
                exit $returnCode
            fi

            cd drools-ant
            $mvnCommand
            returnCode=$?
            if [ $returnCode != 0 ]; then
                cd ../..
                exit $returnCode
            fi
            cd ..

            cd drools-eclipse
            $mvnCommand -N
            returnCode=$?
            if [ $returnCode != 0 ]; then
                cd ../..
                exit $returnCode
            fi
            mvn tycho-versions:set-version -DnewVersion=$newVersion
            returnCode=$?
            if [ $returnCode != 0 ]; then
                cd ../..
                exit $returnCode
            fi
            cd ..

            cd droolsjbpm-tools-distribution
            $mvnCommand
            returnCode=$?
            if [ $returnCode != 0 ]; then
                cd ../..
                exit $returnCode
            fi
            cd ..
        fi

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
