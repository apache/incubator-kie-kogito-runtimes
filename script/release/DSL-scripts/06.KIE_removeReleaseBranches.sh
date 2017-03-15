#!/bin/bash

if [ "$TARGET" == "community" ]; then 
   SOURCE=origin
else
   SOURCE=jboss-integration
fi

BLESSED_BRANCHES=("6.2.x" "6.3.x" "6.4.x" "6.5.x" "7.0.x" "master")

if [[ " ${BLESSED_BRANCHES[*]} "  ==  *"$RELEASE_BRANCH"*  ]]; then
    echo "Branch $BRANCH can't be removed"
    exit 1
else
   echo "$BRANCH will be removed"

   git clone git@github.com:"$SOURCE"/droolsjbpm-build-bootstrap.git --branch $RELEASE_BRANCH --depth 50

   # clone rest of the repos and checkout to this branch
   ./droolsjbpm-build-bootstrap/script/git-clone-others.sh --branch $RELEASE_BRANCH --depth 50

   # remove release-branches on droolsjbpm or on jboss-integration
   ./droolsjbpm-build-bootstrap/script/git-all.sh push $SOURCE :$RELEASE_BRANCH 
fi
