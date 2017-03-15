echo "KIE version" $KIE_VERSION

if [ "$TARGET" == "community" ]; then  
   STAGING_REP=kie-group
else
   STAGING_REP=kie-internal-group
fi

# wget the tar.gz sources
wget -q https://repository.jboss.org/nexus/content/groups/$STAGING_REP/org/kie/kie-wb-distributions/$KIE_VERSION/kie-wb-distributions-$KIE_VERSION-project-sources.tar.gz -O sources.tar.gz

tar xzf sources.tar.gz
mv kie-wb-distributions-$KIE_VERSION/* .
rmdir kie-wb-distributions-$KIE_VERSION
printenv
