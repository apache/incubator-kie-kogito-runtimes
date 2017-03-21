if [ "$TARGET" == "community" ]; then 
   STAGING_REP=kie-group
else
   STAGING_REP=kie-internal-group
fi

echo "KIE version: $KIE_VERSION"
echo "TARGET : $TARGET"

# wget the tar.gz sources
wget -q https://repository.jboss.org/nexus/content/groups/$STAGING_REP/org/jbpm/jbpm/$KIE_VERSION/jbpm-$KIE_VERSION-project-sources.tar.gz -O sources.tar.gz

tar xzf sources.tar.gz
mv jbpm-$KIE_VERSION/* .
rmdir jbpm-$KIE_VERSION
