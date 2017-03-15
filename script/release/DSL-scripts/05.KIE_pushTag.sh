# clone the build-bootstrap that contains the other build scripts
if [ "$TARGET" == "community" ]; then 
   SOURCE=droolsjbpm
else
   SOURCE=jboss-integration
fi

git clone git@github.com:"$SOURCE"/droolsjbpm-build-bootstrap.git --branch $RELEASE_BRANCH --depth 100

# clone rest of the repos and checkout to this branch
./droolsjbpm-build-bootstrap/script/git-clone-others.sh --branch $RELEASE_BRANCH --depth 100

# create a tag
CommitMSG="Tagging $TAG_NAME"
./droolsjbpm-build-bootstrap/script/git-all.sh tag -a $TAG_NAME -m "$CommitMSG"

# pushes tag to the SOURCE
./droolsjbpm-build-bootstrap/script/git-all.sh push origin $TAG_NAME
