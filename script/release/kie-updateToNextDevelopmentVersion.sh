#!/bin/bash -e

target=community
targetUser=kiereleaseuser
targetUserRemote=kie
DATE=$(date "+%Y-%m-%d")
prBranch=UPDATE-VERSION-$DATE

# clone droolsjbm-build-bootstrap branch from kiegroup
git clone git@github.com:kiegroup/droolsjbpm-build-bootstrap.git --branch $baseBranch

# clone rest of the repos
./droolsjbpm-build-bootstrap/script/git-clone-others.sh --branch $baseBranch --depth 70

# checkout to PR_branch
./droolsjbpm-build-bootstrap/script/git-all.sh checkout -b $prBranch $baseBranch

# upgrades the version to the release/tag version
./droolsjbpm-build-bootstrap/script/release/update-version-all.sh $newVersion $target

# change <version.org.uberfire>, <version.org.dashbuilder> and <version.org.jboss.errai>
cd $WORKSPACE/droolsjbpm-build-bootstrap
sed -i "$!N;s/<version.org.uberfire>.*.<\/version.org.uberfire>/<version.org.uberfire>$uberfireDevelVersion<\/version.org.uberfire>/;P;D" pom.xml
sed -i "$!N;s/<version.org.dashbuilder>.*.<\/version.org.dashbuilder>/<version.org.dashbuilder>$dashbuilderDevelVersion<\/version.org.dashbuilder>/;P;D" pom.xml
sed -i "$!N;s/<version.org.jboss.errai>.*.<\/version.org.jboss.errai>/<version.org.jboss.errai>$erraiDevelVersion<\/version.org.jboss.errai>/;P;D" pom.xml

cd $WORKSPACE

# git add and commit the version update changes 
./droolsjbpm-build-bootstrap/script/git-all.sh add .
commitMsg="Upgraded to next development version $newVersion"
./droolsjbpm-build-bootstrap/script/git-all.sh commit -m "$commitMsg"

# add a remote to all repositories
repositoryList=$WORKSPACE/droolsjbpm-build-bootstrap/script/repository-list.txt

for repDir in `cat $repositoryList` ; do

   if [ -d $repDir ]; then
      echo " "  
      echo "==============================================================================="
      echo "Repository: $repDir"
      echo "==============================================================================="
      echo " "
      
      cd $WORKSPACE/$repDir
      
      # adds a remote to kiereleaseuser
      git remote add $targetUserRemote git@github.com:$targetUser/$repDir
      
      source=kiegroup
      
      echo "we are at: "$repDir
      echo "the new remote is: " 
      git remote -v
      git push -f $targetUserRemote $prBranch
      hub pull-request -m "$commitMsg" -b $source:$baseBranch -h $targetUser:$prBranch
   
      cd $WORKSPACE
   fi
done

