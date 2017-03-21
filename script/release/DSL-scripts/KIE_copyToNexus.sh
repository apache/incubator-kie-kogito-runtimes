# fetch the <version.org.kie> from kie-parent-metadata pom.xml and set it on parameter KIE_VERSION
KIE_VERSION=$(sed -e 's/^[ \t]*//' -e 's/[ \t]*$//' -n -e 's/<version.org.kie>\(.*\)<\/version.org.kie>/\1/p' droolsjbpm-build-bootstrap/pom.xml)

if [ "$TARGET" == "community" ]; then 
   STAGING_REP_ID=15c58a1abc895b
   DEPLOY_DIR=$WORKSPACE/KIE_Deploy_dir_community_7.0.x
else
   STAGING_REP_ID=15c3321d12936e
   DEPLOY_DIR=$WORKSPACE/KIE_Deploy_dir_prod_7.0.x
fi

cd $DEPLOY_DIR
# upload the content to remote staging repo
mvn -B -e org.sonatype.plugins:nexus-staging-maven-plugin:1.6.5:deploy-staged-repository -DnexusUrl=https://repository.jboss.org/nexus -DserverId=jboss-releases-repository\
 -DrepositoryDirectory=$DEPLOY_DIR -DstagingProfileId=$STAGING_REP_ID -DstagingDescription="kie-$KIE_VERSION" -DstagingProgressTimeoutMinutes=30

