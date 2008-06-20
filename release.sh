#/bin/sh

#
#  Shows the error message and stops the script execution
#
#  $1 : error message to show
#
show_error() {
    echo 
    echo "ERROR: $1"
    echo
    exit 1
}

#
#  Main script
#
echo "*************************************************************"
echo "------> Importing release configuration"
. release.env
echo "CURRENT_VERSION = $CURRENT_VERSION"
echo "RELEASE_VERSION = $RELEASE_VERSION"
echo "NEW_VERSION     = $NEW_VERSION"
echo "TAG_NAME        = $TAG_NAME"
echo

echo "*************************************************************"
echo "------> Updating release version in configuration files"
echo $ANT -f update-version.xml -Dcurrent="$CURRENT_VERSION" -Dnew="$RELEASE_VERSION" updateVersion
if [ $? -ne 0 ] 
then
    show_error "****** Error updating version numbers. Exiting. ******"
fi
echo

echo "*************************************************************"
echo "------> Commiting new version into trunk"
echo $SVN commit -m "$JIRA_TICKET : preparing release. Updating files from version $CURRENT_VERSION to $RELEASE_VERSION"
if [ $? -ne 0 ] 
then
    show_error "****** Error commiting update files to trunk. Exiting. ******"
fi
echo

echo "*************************************************************"
echo "------> Preparing the release"
echo $MVN --batch-mode release:clean release:prepare 
if [ $? -ne 0 ] 
then
    show_error "****** Error preparing the release. Exiting. ******"
fi
echo

echo "*************************************************************"
echo "------> Generating artifacts"
echo $MVN -Ddocumentation -Declipse -Dmaven.test.skip -Dydoc.home=$YDOC_HOME package javadoc:javadoc assembly:assembly
if [ $? -ne 0 ] 
then
    show_error "****** Error generating distribution artifacts. Exiting. ******"
fi
echo

