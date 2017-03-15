# removing KIE artifacts from local maven repo (basically all possible SNAPSHOTs)
echo $MAVEN_REPO_LOCAL "="

if [ -d $MAVEN_REPO_LOCAL ]; then
    rm -rf $MAVEN_REPO_LOCAL/org/jboss/dashboard-builder/
    rm -rf $MAVEN_REPO_LOCAL/org/kie/
    rm -rf $MAVEN_REPO_LOCAL/org/drools/
    rm -rf $MAVEN_REPO_LOCAL/org/jbpm/
    rm -rf $MAVEN_REPO_LOCAL/org/optaplanner/
    rm -rf $MAVEN_REPO_LOCAL/org/guvnor/
    
fi

# clone the build-bootstrap that contains the other build scripts
if [ "$TARGET" == "community" ]; then 
   SOURCE=droolsjbpm
else
   SOURCE=jboss-integration
fi

git clone git@github.com:"$SOURCE"/droolsjbpm-build-bootstrap.git --branch $RELEASE_BRANCH


# clone rest of the repos and checkout to this branch
./droolsjbpm-build-bootstrap/script/git-clone-others.sh --branch $RELEASE_BRANCH --depth 100

# removes kie.properties if it exists
file="kie.properties"
if [ -f "$file" ]; then
   echo "$file found."
   rm $file
fi

# fetch the <version.org.kie> from kie-parent-metadata pom.xml and set it on parameter KIE_VERSION
KIE_VERSION=$(sed -e 's/^[ \t]*//' -e 's/[ \t]*$//' -n -e 's/<version.org.kie>\(.*\)<\/version.org.kie>/\1/p' droolsjbpm-build-bootstrap/pom.xml)

# creates a properties file to pass variables and moves it to the root directory
echo KIE_VERSION=$KIE_VERSION > kie.properties
echo TARGET=$TARGET >> kie.properties

# build release branches
if [ "$TARGET" == "community" ]; then 
   DEPLOY_DIR=$WORKSPACE/KIE_Deploy_dir_community_7.0.x
   # (1) do a full build, but deploy only into local dir
   # we will deploy into remote staging repo only once the whole build passed (to save time and bandwith)   
   ./droolsjbpm-build-bootstrap/script/mvn-all.sh -B -e -U clean deploy -Dfull -Drelease -T1C -DaltDeploymentRepository=local::default::file://$DEPLOY_DIR -Dmaven.test.failure.ignore=true -Dgwt.memory.settings="-Xmx4g -Xms1g -Xss1M" -Dgwt.compiler.localWorkers=2
  
else
   DEPLOY_DIR=$WORKSPACE/KIE_Deploy_dir_prod_7.0.x
   # (1) do a full build with prod look & feel (-Dproductized), but deploy only into local dir
   # we will deploy into remote staging repo only once the whole build passed (to save time and bandwith)   
   ./droolsjbpm-build-bootstrap/script/mvn-all.sh -B -e -U clean deploy -Dfull -Dproductized -Drelease -T1C -DaltDeploymentRepository=local::default::file://$DEPLOY_DIR -Dmaven.test.failure.ignore=true -Dgwt.memory.settings="-Xmx4g -Xms1g -Xss1M" -Dgwt.compiler.localWorkers=2

fi
