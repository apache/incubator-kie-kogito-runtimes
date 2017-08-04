#!/bin/bash

set -e

whereAmI=$(pwd)
echo $wherAmI

drools-docs=drools@filemgmt.jboss.org:/docs_htdocs/drools/release
drools-htdocs=drools@filemgmt.jboss.org:/downloads_htdocs/drools/release
jbpm-docs=jbpm@filemgmt.jboss.org:/docs_htdocs/jbpm/release
jbpm-htdocs=jbpm@filemgmt.jboss.org:/downloads_htdocs/jbpm/release
optaplanner-docs=optaplanner@filemgmt.jboss.org:/docs_htdocs/optaplanner/release
optaplanner-htdocs=optaplanner@filemgmt.jboss.org:/downloads_htdocs/optaplanner/release


# create directory on filemgmt.jboss.org for new release
touch upload_version
echo "mkdir" $version > upload_version
chmod +x upload_version


sftp -b upload_version $drools-docs
sftp -b upload_version $drools-htdocs
sftp -b upload_version $jbpm-docs
sftp -b upload_version $jbpm-htdocs
sftp -b upload_version $optaplanner-docs
sftp -b upload_version $optaplanner-htdocs

#creates directories for updatesite for drools and jbpm on filemgmt.jboss.org
touch upload_drools
echo "mkdir org.drools.updatesite" > upload_drools
chmod +x upload_drools
sftp -b upload_drools $drools-htdocs/$version

touch upload_jbpm
echo "mkdir updatesite" > upload_jbpm
chmod +x upload_jbpm
sftp -b upload_jbpm $jbpm-htdocs/$version


#creates directories for docs for drools and jbpm and optaplanner on filemgmt.jboss.org
touch upload_drools_docs
echo "mkdir drools-docs" > upload_drools_docs
chmod +x upload_drools_docs
sftp -b upload_drools_docs $drools-docs/$version/

touch upload_kie_api_javadoc
echo "mkdir kie-api-javadoc" > upload_kie_api_javadoc
chmod +x upload_kie_api_javadoc
sftp -b upload_kie_api_javadoc $drools-docs/$version

touch upload_jbpm_docs
echo "mkdir jbpm-docs" > upload_jbpm_docs
chmod +x upload_jbpm_docs
sftp -b upload_jbpm_docs $jbpm-docs/$version

touch upload_optaplanner_docs
echo "mkdir optaplanner-docs" > upload_optaplanner_docs
chmod +x upload_optaplanner_docs
sftp -b upload_optaplanner_docs $optaplanner-docs/$version

touch upload_optaplanner_javadoc
echo "mkdir optaplanner-javadoc" > upload_optaplanner_javadoc
chmod +x upload_optaplanner_javadoc
sftp -b upload_optaplanner_javadoc $optaplanner-docs/$version

touch upload_optaplanner_wb_es_docs
echo "mkdir optaplanner-wb-es-docs" > upload_optaplanner_wb_es_docs
chmod +x upload_optaplanner_wb_es_docs
sftp -b upload_optaplanner_wb_es_docs $optaplanner-docs/$version

# copies drools binaries to filemgmt.jboss.org
scp -r droolsjbpm-tools/droolsjbpm-tools-distribution/target/droolsjbpm-tools-distribution-$version/droolsjbpm-tools-distribution-$version/binaries/org.drools.updatesite/* $drools-htdocs/$version/org.drools.updatesite
scp drools/drools-distribution/target/drools-distribution-$version.zip $drools-htdocs/$version
scp droolsjbpm-integration/droolsjbpm-integration-distribution/target/droolsjbpm-integration-distribution-$version.zip $drools-htdocs/$version
scp droolsjbpm-tools/droolsjbpm-tools-distribution/target/droolsjbpm-tools-distribution-$version.zip $drools-htdocs/$version
scp kie-wb-distributions/kie-drools-wb-parent/kie-drools-wb-distribution-wars/target/kie-drools-wb-$version-*.war $drools-htdocs/$version
scp drools-wb/drools-wb-jcr2vfs-migration/drools-wb-jcr2vfs-distribution/target/drools-wb-jcr2vfs-distribution-$version.zip $drools-htdocs/$version
scp droolsjbpm-integration/kie-server-parent/kie-server-wars/kie-server-distribution/target/kie-server-distribution-$version.zip $drools-htdocs/$version

#copies drools-docs and kie-api-javadoc to filemgmt.jboss.or
scp -r kie-docs/docs/drools-docs/target/generated-docs/* $drools-docs/$version/drools-docs
scp -r droolsjbpm-knowledge/kie-api/target/apidocs/* $drools-docs/$version/kie-api-javadoc

#copies jbpm binaries to filemgmt.jboss.org
scp -r droolsjbpm-tools/droolsjbpm-tools-distribution/target/droolsjbpm-tools-distribution-$version/droolsjbpm-tools-distribution-$version/binaries/org.drools.updatesite/* $jbpm-htdocs/$version/updatesite
scp jbpm/jbpm-distribution/target/jbpm-$version-bin.zip $jbpm-htdocs/$version
scp jbpm/jbpm-installer/target/jbpm-installer-$version.zip $jbpm-htdocs/$version
scp jbpm/jbpm-distribution/target/jbpm-$version-examples.zip $jbpm-htdocs/$version

#copies jbpm-docs to filemgmt.jboss.org
scp -r kie-docs/docs/jbpm-docs/target/generated-docs/* $jbpm-docs/$version/jbpm-docs

#copies optaplanner binaries to filemgmt.jboss.org
scp optaplanner/optaplanner-distribution/target/optaplanner-distribution-$version.zip $optaplanner-htdocs/$version

#copies optaplanner-docs and optaplanner-javadoc to filemgmt.jboss.org
scp -r optaplanner/optaplanner-docs/target/generated-docs/* $optaplanner-docs/$version/optaplanner-docs
scp -r optaplanner/optaplanner-distribution/target/optaplanner-distribution-$version/optaplanner-distribution-$version/javadocs/* $optaplanner-docs/$version/optaplanner-javadoc
scp -r kie-docs/docs/optaplanner-wb-es-docs/target/generated-docs/* $optaplanner-docs/$version/optaplanner-wb-es-docs

# clean upload files
rm upload_*

# runs create_filemgmt_links.sh
sh droolsjbpm-build-bootstrap/script/release/create_filemgmt_links.sh $version
