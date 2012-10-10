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

# TODO move jbpm from sourceforge to filemgmt
withoutJbpm=true
if [ $# != 1 ] ; then
    echo "ERROR jbpm is not supported yet!"
    exit 1
fi

if [ $# != 1 ] && [ $# != 2 ] ; then
    echo
    echo "Usage:"
    echo "  $0 droolsVersion [jbpmVersion]"
    echo "For example:"
    echo "  $0 5.2.0.Final 5.1.0.Final"
    echo
    exit 1
fi
droolsVersion=$1
echo "The drools, guvnor, ... version: $droolsVersion"
if [ "$withoutJbpm" != 'true' ]; then
    jbpmVersion=$2
    echo "The jbpm version: $jbpmVersion"
fi
echo -n "Is this ok? (Hit control-c if is not): "
read ok

cd $droolsjbpmOrganizationDir/droolsjbpm-build-distribution/droolsjbpm-uber-distribution
cd target
mkdir filemgmt_links
cd filemgmt_links

urlBase="drools@filemgmt.jboss.org:"

touch $droolsVersion
rm latest
ln -s $droolsVersion latest
echo "Uploading normal links..."
rsync -a --protocol=28  latest $urlBase/downloads_htdocs/drools/release/
rsync -a --protocol=28  latest $urlBase/docs_htdocs/drools/release/
# TODO JBRULES-3470 but the "ln " trick does not work from a normal developer machine
# echo "ln ../../../../../../drools/release/$droolsVersion/org.drools.updatesite $droolsVersion" | sftp $urlBase/downloads_htdocs/tools/updates/stable/indigo/soa-tooling/droolsjbpm/
if [[ "$droolsVersion" == *Final* ]]; then
    rm latestFinal
    ln -s $droolsVersion latestFinal
    echo "Uploading Final links..."
    rsync -a --protocol=28  latestFinal $urlBase/downloads_htdocs/drools/release/
    rsync -a --protocol=28  latestFinal $urlBase/docs_htdocs/drools/release/
fi
