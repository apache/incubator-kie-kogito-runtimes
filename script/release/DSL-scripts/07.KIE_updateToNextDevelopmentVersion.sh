TARGET=community
TARGET_USER=kiereleaseuser
TARGET_USER_REMOTE=kie
DATE=$(date "+%Y-%m-%d")
PR_BRANCH=PR_BRANCH_$DATE

# clone droolsjbm-build-bootstrap branch from kiegroup
git clone git@github.com:kiegroup/droolsjbpm-build-bootstrap.git --branch $BASE_BRANCH

# clone rest of the repos
./droolsjbpm-build-bootstrap/script/git-clone-others.sh --branch $BASE_BRANCH --depth 70

# checkout to PR_branch
./droolsjbpm-build-bootstrap/script/git-all.sh checkout -b $PR_BRANCH $BASE_BRANCH

# upgrades the version to the release/tag version
sh script/release/update-version-all.sh $newVersion $TARGET

# change <version.org.uberfire>, <version.org.dashbuilder> and <version.org.jboss.errai>
sed -i "$!N;s/<version.org.uberfire>.*.<\/version.org.uberfire>/<version.org.uberfire>$UBERFIRE_VERSION<\/version.org.uberfire>/;P;D" pom.xml
sed -i "$!N;s/<version.org.dashbuilder>.*.<\/version.org.dashbuilder>/<version.org.dashbuilder>$DASHBUILDER_VERSION<\/version.org.dashbuilder>/;P;D" pom.xml
sed -i "$!N;s/<version.org.jboss.errai>.*.<\/version.org.jboss.errai>/<version.org.jboss.errai>$ERRAI_VERSION<\/version.org.jboss.errai>/;P;D" pom.xml

cd $WORKSPACE

# git add and commit the version update changes 
sh script/git-all.sh add .
CommitMSG="Upgraded to next development version $newVersion"
sh script/git-all.sh commit -m "$CommitMSG"

# add a remote to all repositories
REPOSITORY_LIST=$WORKSPACE/droolsjbpm-build-bootstrap/script/repository-list.txt

for REP_DIR in `cat $REPOSITORY_LIST` ; do

   if [ -d $REP_DIR ]; then
      echo " "  
      echo "==============================================================================="
      echo "Repository: $REP_DIR"
      echo "==============================================================================="
      echo " "
      
      cd $WORKSPACE/$REP_DIR
      
      # adds a remote to kiereleaseuser
      git remote add $TARGET_USER_REMOTE git@github.com:$TARGET_USER/$REP_DIR
      
      SOURCE=kiegroup
      
      echo "we are at: "$REP_DIR
      echo "the new remote is: " 
      git remote -v
      git push -f $TARGET_USER_REMOTE $PR_BRANCH
      hub pull-request -m "$CommitMSG" -b $SOURCE:$BASE_BRANCH -h $TARGET_USER:$PR_BRANCH
   
      cd $WORKSPACE
   fi
done

