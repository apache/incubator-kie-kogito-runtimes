#!/bin/bash

set -e

WHERE_AM_I=$(pwd)
echo $WHERE_AM_I

DROOLS_DOCS=drools@filemgmt.jboss.org:/docs_htdocs/drools/release
DROOLS_HTDOCS=drools@filemgmt.jboss.org:/downloads_htdocs/drools/release
JBPM_DOCS=jbpm@filemgmt.jboss.org:/docs_htdocs/jbpm/release
JBPM_HTDOCS=jbpm@filemgmt.jboss.org:/downloads_htdocs/jbpm/release
OPTAPLANNER_DOCS=optaplanner@filemgmt.jboss.org:/docs_htdocs/optaplanner/release
OPTAPLANNER_HTDOCS=optaplanner@filemgmt.jboss.org:/downloads_htdocs/optaplanner/release


# create directory on filemgmt.jboss.org for new release
touch upload_version
echo "mkdir" $VERSION > upload_version
chmod +x upload_version


sftp -b upload_version $DROOLS_DOCS
sftp -b upload_version $DROOLS_HTDOCS 
sftp -b upload_version $JBPM_DOCS
sftp -b upload_version $JBPM_HTDOCS
sftp -b upload_version $OPTAPLANNER_DOCS
sftp -b upload_version $OPTAPLANNER_HTDOCS

#creates directories for updatesite for drools and jbpm on filemgmt.jboss.org
touch upload_drools
echo "mkdir org.drools.updatesite" > upload_drools
chmod +x upload_drools
sftp -b upload_drools $DROOLS_HTDOCS/$VERSION

touch upload_jbpm
echo "mkdir updatesite" > upload_jbpm
chmod +x upload_jbpm
sftp -b upload_jbpm $JBPM_HTDOCS/$VERSION


#creates directories for docs for drools and jbpm and optaplanner on filemgmt.jboss.org
touch upload_drools_docs
echo "mkdir drools-docs" > upload_drools_docs
chmod +x upload_drools_docs
sftp -b upload_drools_docs $DROOLS_DOCS/$VERSION/

touch upload_kie_api_javadoc
echo "mkdir kie-api-javadoc" > upload_kie_api_javadoc
chmod +x upload_kie_api_javadoc
sftp -b upload_kie_api_javadoc $DROOLS_DOCS/$VERSION

touch upload_jbpm_docs
echo "mkdir jbpm-docs" > upload_jbpm_docs
chmod +x upload_jbpm_docs
sftp -b upload_jbpm_docs $JBPM_DOCS/$VERSION

touch upload_optaplanner_docs
echo "mkdir optaplanner-docs" > upload_optaplanner_docs
chmod +x upload_optaplanner_docs
sftp -b upload_optaplanner_docs $OPTAPLANNER_DOCS/$VERSION

touch upload_optaplanner_javadoc
echo "mkdir optaplanner-javadoc" > upload_optaplanner_javadoc
chmod +x upload_optaplanner_javadoc
sftp -b upload_optaplanner_javadoc $OPTAPLANNER_DOCS/$VERSION

# copies drools binaries to filemgmt.jboss.org
scp -r droolsjbpm-tools/droolsjbpm-tools-distribution/target/droolsjbpm-tools-distribution-$VERSION/droolsjbpm-tools-distribution-$VERSION/binaries/org.drools.updatesite/* $DROOLS_HTDOCS/$VERSION/org.drools.updatesite  
scp drools/drools-distribution/target/drools-distribution-$VERSION.zip $DROOLS_HTDOCS/$VERSION
scp droolsjbpm-integration/droolsjbpm-integration-distribution/target/droolsjbpm-integration-distribution-$VERSION.zip $DROOLS_HTDOCS/$VERSION
scp droolsjbpm-tools/droolsjbpm-tools-distribution/target/droolsjbpm-tools-distribution-$VERSION.zip $DROOLS_HTDOCS/$VERSION
scp kie-wb-distributions/kie-drools-wb-parent/kie-drools-wb-distribution-wars/target/kie-drools-wb-$VERSION-*.war $DROOLS_HTDOCS/$VERSION
scp drools-wb/drools-wb-jcr2vfs-migration/drools-wb-jcr2vfs-distribution/target/drools-wb-jcr2vfs-distribution-$VERSION.zip $DROOLS_HTDOCS/$VERSION 
scp droolsjbpm-integration/kie-server-parent/kie-server-wars/kie-server-distribution/target/kie-server-distribution-$VERSION.zip $DROOLS_HTDOCS/$VERSION

#copies drools-docs and kie-api-javadoc to filemgmt.jboss.or
scp -r kie-docs/docs/drools-docs/target/generated-docs/* $DROOLS_DOCS/$VERSION/drools-docs
scp -r droolsjbpm-knowledge/kie-api/target/apidocs/* $DROOLS_DOCS/$VERSION/kie-api-javadoc

#copies jbpm binaries to filemgmt.jboss.org
scp -r droolsjbpm-tools/droolsjbpm-tools-distribution/target/droolsjbpm-tools-distribution-$VERSION/droolsjbpm-tools-distribution-$VERSION/binaries/org.drools.updatesite/* $JBPM_HTDOCS/$VERSION/updatesite
scp jbpm/jbpm-distribution/target/jbpm-$VERSION-bin.zip $JBPM_HTDOCS/$VERSION
scp jbpm/jbpm-distribution/target/jbpm-$VERSION-installer.zip $JBPM_HTDOCS/$VERSION
scp jbpm/jbpm-distribution/target/jbpm-$VERSION-examples.zip $JBPM_HTDOCS/$VERSION

#copies jbpm-docs to filemgmt.jboss.org
scp -r kie-docs/docs/jbpm-docs/target/generated-docs/* $JBPM_DOCS/$VERSION/jbpm-docs

#copies optaplanner binaries to filemgmt.jboss.org
scp optaplanner/optaplanner-distribution/target/optaplanner-distribution-$VERSION.zip $OPTAPLANNER_HTDOCS/$VERSION

#copies optaplanner-docs and optaplanner-javadoc to filemgmt.jboss.org
scp -r optaplanner/optaplanner-docs/target/generated-docs/* $OPTAPLANNER_DOCS/$VERSION/optaplanner-docs
scp -r optaplanner/optaplanner-distribution/target/optaplanner-distribution-$VERSION/optaplanner-distribution-$VERSION/javadocs/* $OPTAPLANNER_DOCS/$VERSION/optaplanner-javadoc

# clean upload files
rm upload_*

# runs create_filemgmt_links.sh
sh droolsjbpm-build-bootstrap/script/release/create_filemgmt_links.sh $VERSION
