#!/bin/bash -e

echo "kieVersion:" $kieVersion
echo "target" : $target

# clone the build-bootstrap that contains the other build scripts
if [ "$target" == "community" ]; then
   git clone git@github.com:kiegroup/droolsjbpm-build-bootstrap.git --branch $releaseBranch --depth 70
else
   git clone ssh://jb-ip-tooling-jenkins@code.engineering.redhat.com/kiegroup/droolsjbpm-build-bootstrap --branch $releaseBranch --depth 70
fi

# clone rest of the repos and checkout to this branch
./droolsjbpm-build-bootstrap/script/git-clone-others.sh --branch $releaseBranch --depth 70

# create a tag
commitMsg="Tagging $tag"
./droolsjbpm-build-bootstrap/script/git-all.sh tag -a $tag -m "$commitMsg"

# pushes tag to kiegroup or gerrit
if [ "$target" == "community" ]; then
   ./droolsjbpm-build-bootstrap/script/git-all.sh push origin $tag
else
   # create remote pointing to Gerrit
   ./droolsjbpm-build-bootstrap/script/git-remote-add-gerrit.sh
   ./droolsjbpm-build-bootstrap/script/git-all.sh push gerrit $tag
fi
