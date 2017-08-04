#!/bin/bash -e

# clone droolsjbpm-build-bootstrap
git clone git@github.com:kiegroup/droolsjbpm-build-bootstrap.git --branch $baseBranch --depth 70

# clone rest of the repos
./droolsjbpm-build-bootstrap/script/git-clone-others.sh --branch $baseBranch --depth 70

if [ "$source" == "community-branch" ]; then

   # checkout to local release names
   ./droolsjbpm-build-bootstrap/script/git-all.sh checkout -b $releaseBranch $baseBranch
   
   # add new remote pointing to jboss-integration
   ./droolsjbpm-build-bootstrap/script/git-add-remote-jboss-integration.sh

fi

if [ "$source" == "community-tag" ]; then

   # add new remote pointing to jboss-integration
   ./droolsjbpm-build-bootstrap/script/git-add-remote-jboss-integration.sh
   
   # get the tags of community
   ./droolsjbpm-build-bootstrap/script/git-all.sh fetch --tags origin
   
   # checkout to local release names
   ./droolsjbpm-build-bootstrap/script/git-all.sh checkout -b $releaseBranch $tag

fi
   
if [ "$source" == "production-tag" ]; then

   # add new remote pointing to jboss-integration
   ./droolsjbpm-build-bootstrap/script/git-add-remote-jboss-integration.sh
   
   # get the tags of jboss-integration
   ./droolsjbpm-build-bootstrap/script/git-all.sh fetch --tags jboss-integration
   
   # checkout to local release names
   ./droolsjbpm-build-bootstrap/script/git-all.sh checkout -b $releaseBranch $tag

fi

# upgrades the version to the release/tag version
./droolsjbpm-build-bootstrap/script/release/update-version-all.sh $releaseVersion $target

# update kie-parent-metadata
cd droolsjbpm-build-bootstrap/

# change properties via sed as they don't update automatically
sed -i \
-e "$!N;s/<version.org.uberfire>.*.<\/version.org.uberfire>/<version.org.uberfire>$uberfireVersion<\/version.org.uberfire>/;" \
-e "s/<version.org.dashbuilder>.*.<\/version.org.dashbuilder>/<version.org.dashbuilder>$dashbuilderVersion<\/version.org.dashbuilder>/;" \
-e "s/<version.org.jboss.errai>.*.<\/version.org.jboss.errai>/<version.org.jboss.errai>$erraiVersion<\/version.org.jboss.errai>/;" \
-e "s/<latestReleasedVersionFromThisBranch>.*.<\/latestReleasedVersionFromThisBranch>/<latestReleasedVersionFromThisBranch>$releaseVersion<\/latestReleasedVersionFromThisBranch>/;P;D" \
pom.xml

cd $WORKSPACE

# git add and commit the version update changes 
./droolsjbpm-build-bootstrap/script/git-all.sh add .
commitMsg="Upgraded versions for release $releaseVersion"
./droolsjbpm-build-bootstrap/script/git-all.sh commit -m "$commitMsg"

# pushes the local release branches to kiegroup or to jboss-integration [IMPORTANT: "push -n" (--dryrun) should be replaced by "push" when script will be in production]
if [ "$target" == "community" ]; then
  ./droolsjbpm-build-bootstrap/script/git-all.sh push origin $releaseBranch
else
  ./droolsjbpm-build-bootstrap/script/git-all.sh push jboss-integration $releaseBranch
  ./droolsjbpm-build-bootstrap/script/git-all.sh push jboss-integration $baseBranch
fi
