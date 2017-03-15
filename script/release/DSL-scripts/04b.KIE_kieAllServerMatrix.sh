echo "KIE version" $KIE_VERSION
if [ "$TARGET" == "community" ]; then 
   STAGING_REP=kie-group
else
   STAGING_REP=kie-internal-group
fi
pwd
echo "WORKSPACE :" $WORKSPACE
# wget the tar.gz sources
wget -q https://repository.jboss.org/nexus/content/groups/$STAGING_REP/org/drools/droolsjbpm-integration/$KIE_VERSION/droolsjbpm-integration-$KIE_VERSION-project-sources.tar.gz -O sources.tar.gz

tar xzf sources.tar.gz
mv droolsjbpm-integration-$KIE_VERSION/* .
rmdir droolsjbpm-integration-$KIE_VERSION
