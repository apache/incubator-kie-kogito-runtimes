#/bin/sh

#
#  Checks if there is an error flag active and if so,
#  shows an error message and terminates the script
#
#  $1 : error message to show
#
check_error() {
    if [ $? -ne 0 ] 
    then
        echo 
        echo "ERROR: $1"
        echo
        exit 1
    fi
}

#
#  Generate the release file
#
#  $1 : Release version
#  $2 : Development version
#  $3 : Release Tag
#
create_release_file() {
    REL_VERSION=$1
    DEV_VERSION=$2
    TAG_VERSION=$3
    
    cat > release.properties <<EOF 
#release configuration
#Fri Jun 20 12:51:50 BRT 2008
scm.tag=$TAG_VERSION
project.rel.org.drools\:drools-ant=$REL_VERSION
project.dev.org.drools\:drools-templates=$DEV_VERSION
project.dev.org.drools\:drools-clips=$DEV_VERSION
project.dev.org.drools.solver\:drools-solver=$DEV_VERSION
project.dev.org.drools\:drools-repository=$DEV_VERSION
project.rel.org.drools\:drools-repository=$REL_VERSION
project.rel.org.drools.solver\:drools-solver-core=$REL_VERSION
project.rel.org.drools\:drools-clips=$REL_VERSION
project.rel.org.drools.solver\:drools-solver=$REL_VERSION
project.dev.org.drools\:drools-decisiontables=$DEV_VERSION
project.dev.org.drools.solver\:drools-solver-examples=$DEV_VERSION
project.dev.org.drools\:drools-compiler=$DEV_VERSION
project.rel.org.drools\:drools-core=$REL_VERSION
project.dev.org.drools\:drools-verifier=$DEV_VERSION
project.dev.org.drools\:drools-guvnor=$DEV_VERSION
project.rel.org.drools\:drools-compiler=$REL_VERSION
project.dev.org.drools\:drools-ant=$DEV_VERSION
project.rel.org.drools\:drools-verifier=$REL_VERSION
project.rel.org.drools\:drools-decisiontables=$REL_VERSION
project.rel.org.drools\:drools-templates=$REL_VERSION
project.dev.org.drools\:drools-jsr94=$DEV_VERSION
project.rel.org.drools\:drools-guvnor=$REL_VERSION
project.dev.org.drools.solver\:drools-solver-core=$DEV_VERSION
project.rel.org.drools\:drools=$REL_VERSION
project.rel.org.drools\:drools-jsr94=$REL_VERSION
project.dev.org.drools\:drools=$DEV_VERSION
project.dev.org.drools\:drools-core=$DEV_VERSION
project.rel.org.drools.solver\:drools-solver-examples=$REL_VERSION
EOF
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
$ANT -f update-version.xml -Dcurrent="$CURRENT_VERSION" -Dnew="$RELEASE_VERSION" updateVersion
check_error "****** Error updating version numbers. Exiting. ******"
echo

echo "*************************************************************"
echo "------> Commiting new version into trunk"
$SVN commit -m "$JIRA_TICKET : preparing release. Updating files from version $CURRENT_VERSION to $RELEASE_VERSION"
check_error "****** Error commiting update files to trunk. Exiting. ******"
echo

echo "*************************************************************"
echo "------> Preparing the release"
$MVN release:clean
check_error "****** Error cleaning up for the release. Exiting. ******"
create_release_file $RELEASE_VERSION $NEW_VERSION $TAG_NAME
$MVN --batch-mode release:prepare 
check_error "****** Error preparing the release. Exiting. ******"
echo

echo "*************************************************************"
echo "------> Generating artifacts"
echo $MVN -Drelease=true -Dmaven.test.skip -Dydoc.home=$YDOC_HOME -DlocalEclipseDrop=/home/hudson/configs/jboss-rules/local-eclipse-drop-mirror -Drules.site.deploy.dir=file://$WORKSPACE/rules-ouput package site:site site:deploy javadoc:javadoc assembly:assembly
check_error "****** Error generating distribution artifacts. Exiting. ******"
echo
 
echo "*************************************************************"
echo "------> Uploading artifacts"
mkdir target/$RELEASE_VERSION
mv target/drools*.zip target/$RELEASE_VERSION
check_error "****** Error preparing artifacts for upload. Exiting. ******"
scp -Br -i ~/.ssh/id_rsa target/$RELEASE_VERSION jbossqa@downloads.jboss.com:htdocs/drools/release/$RELEASE_VERSION
check_error "****** Error uploading artifacts. Exiting. ******"
scp -Br -i ~/.ssh/id_rsa target/site jbossqa@downloads.jboss.com:htdocs/drools/docs/$RELEASE_VERSION
check_error "****** Error uploading documentation. Exiting. ******"
echo



