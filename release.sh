#/bin/sh

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
echo

echo "*************************************************************"
echo "------> Commiting new version into trunk"
echo $SVN commit -m "$JIRA_TICKET : preparing release. Updating files from version $CURRENT_VERSION to $RELEASE_VERSION"
echo

echo "*************************************************************"
echo "------> Preparing the release"
echo $MVN --batch-mode release:clean release:prepare 

echo "*************************************************************"
echo "------> Generating artifacts"
echo $MVN -Ddocumentation -Declipse -Dmaven.test.skip -Dydoc.home=$YDOC_HOME package javadoc:javadoc assembly:assembly
