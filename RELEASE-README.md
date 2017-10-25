Table of content
================
* **[Releasing](#releasing)**

* **[Building a Product Tag](#building-a-product-tag)**

Releasing
=========

Expecting a release
-------------------

One week in advance:

* Announce on the upcoming release on all the developer mailing lists and in the IRC channel topics.

    * Include a list of projects on Jenkins that are yellow or red.

        * Daily remind the lead of any project that is red.

    * For a CR/Final, also mention the FindBugs reports on Jenkins.

* All external dependencies must be on a non-SNAPSHOT version, to avoid failing to *close* the staging repo on nexus near the end of the release.

    * Get those dependencies (uberfire, uberfire-extensions, dashbuilder) released if needed, preferably 1 week before the kie release. This way, those released artifacts gets tested by our tests.

* Ask kie-wb module (kie-uberfire-extensions, uberfire, kie-wb-common, drools-wb, jbpm-console-ng, jbpm-designer, jbpm-dashboard, dashboard-builder, and kie-wb-distributions) leads to update the translations with Zanata:

    * Translations into different locales are handled within Zanata (https://translate.jboss.org/)

    * Email Zanata mailing list that a release is about to be made.

    * The most recent translations need to be pulled into the release branch. Assuming you have set-up your Zanata configuration correctly, this can be achieved with:

        ```shell
        $ mvn zanata:pull-module
        ```

    * NOTE: If releasing a new version number (major, minor or micro) a new version of the translations should be setup in Zanata.

    * Automatically fix simple errors in the translations using the following:

        ```shell
        $ mvn replacer:replace -N
        ```

    * NOTE: For the repositories kie-wb-distributions it has to be added to the workflow

        ```shell
        $ mvn native2ascii:native2ascii
        ```

    * NOTE: jbpm-designer has it's own workflow

        ```shell
        $ cd ../jbpm-designer
        $ mvn zanata:pull-module
        $ mvn replacer:replace-N
        $ mvn native2ascii:native2ascii
        $ cd jbpm-designer-api
        $ mvn replacer:replace-N
        ```

    * Zanata workflow is:

        ```shell
        $ mvn zanata:pull-module
        $ mvn relacer:replace-N
        $ mvn native2ascii:native2ascii # In repositories where this has to be executed, please
                                        # pay attention to jbpm-designer.
        $ mvn clean install -Dfull -DskipTests # To see if everything compiles after Zanata changes were pulled.
                                               # In kie-wb-distribution has to be added -Dcustom-container for preventing
                                               # not building the repo cause possibly hanging at kie-smoke-tests
        $ git commit -a # add & commit the changes
        $ git push <upstream> <branch> # push changes to blessed repository
        ```

    * when compiling guvnor, check if there are no other translation issues.

        ```shell
        $ mvn clean install -Dfull -DskipTests
        ```

        * Sometime the variable place-holders {0}, {1}... are missing.

        * Append missing variable place-holders {0}, {1}... to the end of the translated text and email the Zanata mailing list.


* Get access to `filemgmt.jboss.org`

    * Note: This is for internal Red Hat developer information only and requires access to Red Hat's VPN.

    * See [https://docspace.corp.redhat.com/docs/DOC-35393](https://docspace.corp.redhat.com/docs/DOC-35393)

    * Create ssh Key (if not already done)

        * Key must:

            * be RSA-2 ( default for many keygen apps )
            * have 1024+ bit ( 2048 is preferred )
            * have comment with user email address

        * Using many keygen tools the following command will work

                $ ssh-keygen -C your@email.com -b 2048

            * enter key name
            * enter passcode you want

        * Send ticket to IT

            * Have it forwarded to https://engineering.redhat.com/rt/Ticket/Create.html?Queue=58 (RT3 eng-ops-mw) queue
            * Specify that you would like access to drools@filemgmt.jboss.org
            * Attach the *.pub that you created above

48 hours in advance:

* Push deadline: Announce on the upcoming push deadline on all the developer mailing lists and in the IRC channel topics.

    * Commits pushed before the deadline will make the release, the rest won't.

* Pull the latest changes.

    ```shell
    $ git-all.sh pull --rebase
    ```

* Do a sanity check.

    * Produce the distribution zips, build with `-Dfull`:

        ```shell
        $ droolsjbpm-build-bootstrap/script/mvn-all.sh clean install -Dfull -Dcustom-container -DskipTests
        ```

        * Warning: It is not uncommon to run out of either PermGen space or Heap Space. The following settings are known (@Sept-2012) to work:-

            ```shell
            $ export MAVEN_OPTS='-Xms512m -Xmx2200m -XX:MaxPermSize=512m'
            ```

        * Warning: Verify that workspace contains no uncommitted changes or rogue module directories of older branches:

            ```shell
            $ droolsjbpm-build-bootstrap/script/git-all.sh status
            ```

            * Specifically watch out for an uncommitted `*/target` directory: that's the result of a build of an older branch that didn't get cleaned.

                * If the root of that directory gets zipped, binaries of that older branch leak into today's distribution zip.

    * Do a sanity check of the artifacts by running each runExamples.sh from the zips.

        * Go to `kie-wb-distributions/droolsjbpm-uber-distribution/target/*/download_jboss_org`:

            * Unzip the zips to a temporary directory.

            * Start the `runExamples.sh` script for drools, droolsjbpm-integration and optaplanner

            * Deploy the guvnor WildFly 8 war and surf to it:

                * Install the mortgages examples, build it and run the test scenario's

            * Verify that the reference manuals open in a browser (HTML) and Adobe Reader (PDF).

Creating a release branch
-------------------------

A release branch name should always end with `.x` so it looks different from a tag name and a topic branch name.

* When do we create a release branch?

    * We only create a release branch just before releasing CR1.

        * For example, just before releasing 6.1.0.CR1, we created the release branch 6.1.x

            * The release branch 6.2.x contained the releases 6.2.0.CR1, 6.2.0.Final, 6.2.1.Final, 6.2.2.Final, ...

    * Alpha/Beta releases are released directly from master, because we don't backport commits to Alpha/Beta's.

* Alert the IRC dev channels that you're going to branch master.

* Pull the latest changes.

    ```shell
    $ git-all.sh pull --rebase
    ```

* Simply use the script `script/release/create-release-branches.sh` with the drools and jbpm *release branch name*:

    ```shell
    $ droolsjbpm-build-bootstrap/script/release/create-release-branches.sh 6.2.x 6.2.x
    # where 6.2.x is the drools and 6.2.x is the jbpm release branch name
    ```

    * Note: this script creates a release branch, pushes it to origin and sets the upstream from local release branch to remote release branch


* Switch back and forth from master to the release branches for all git repositories

    * If you haven't made the branches yourself, first make sure your local repository knows about them:

        ```shell
        $ droolsjbpm-build-bootstrap/script/git-all.sh fetch
        ```

    * Switch to master with `script/git-checkout-all.sh`

        ```shell
        $ droolsjbpm-build-bootstrap/script/git-checkout-all.sh master master
        ```

    * Update master to the next SNAPSHOT version to avoid clashing the artifacts on nexus of master and the release branch:

        ```shell
        $ droolsjbpm-build-bootstrap/script/release/update-version-all.sh 6.2.0-SNAPSHOT 6.3.0-SNAPSHOT
        ```

        * Note: the arguments are `releaseOldVersion` `releaseNewVersion`

        * WARNING: FIXME the `update-version-all.sh` script does not work correctly if you are releasing a hotfix version.

        * WARNING: `jbpm/pom.xml` sometimes has properties defined that override the `${version.org.jbpm}`. Check this is not the case.

            ```shell
            $ grep -r '6.2.0-SNAPSHOT' **/pom.xml
            # or
            $ for i in $(find . -name "pom.xml"); do grep '6.2.0-SNAPSHOT' $i; done
            ```

        * WARNING: script update-version-all.sh did not update all versions in all modules for 6.3.0-SNAPSHOT. Check all have been updated with the following and re-run if required.

            ```shell
            $ grep -r '6.3.0-SNAPSHOT' **/pom.xml
            # or
            $ for i in $(find . -name "pom.xml"); do grep '6.3.0-SNAPSHOT' $i; done
            ```
            or
            ```shell
            $ grep -ER --exclude-dir=*git* --exclude-dir=*target* --exclude-dir=*idea* --exclude=*ipr --exclude=*iws --exclude=*iml --exclude=workspace* --exclude-dir=*.errai 6.3.0-SNAPSHOT . | grep -v ./kie-wb-distributions/kie-eap-integration/kie-eap-modules/kie-jboss-eap-base-modules
            ```

        * Note: in either case it is important to search for `-SNAPSHOT`, as there are various hidden `-SNAPSHOT` dependencies in some pom.xml files and they should be prevented for releases

        * IMPORTANT: Right now the script is not updating automatically all poms of droolsjbpm-tools.
          This could be the case when the number of release i.e. 6.1.0 changes to 6.1.1.

          When the change is in the appendix only (i.e. Beta, CR, Final) the scripts should work correctly. There is the file droolsjbpm-tools/drools-eclipse/org.drools.updatesite/category.xml that has to be updated manually if the script doesn't run correctly.

          Steps to do it working:

            1. run script droolsjbpm-build-bootstrap/script/release/update-version-all
            2. since this script will fail in droolsjbpm-tools edit droolsjbpm-tools/drools-eclipse/org.drools.updatesite/category.xml and modify manually all *.feature.source_***.qualifier
               Don't do this before you did the first run that fails - you have to run the script first!
            3. re-run droolsjbpm-build-bootstrap/script/release/update-version-all

        * NOTE: the repository fuse-bxms-integ has to be upgraded manually

        * Commit those changes (so you can tag them properly):

            * Add changes from untracked files if there are any. WARNING: DO NOT USE `git add .`. You may accidentally add files that are not meant to be added into git.

                ```shell
                $ git add {filename}
                ```

            * Commit all changes

                ```shell
                $ droolsjbpm-build-bootstrap/script/git-all.sh commit -m "Set release version: 6.3.0-SNAPSHOT"
                ```

            * Check if all repositories build after version upgrade

                ```shell
                $ sh droolsjbpm-build-bootstrap/mvnall.sh mvn clean install -Dfull -DskipTests
                ```

    * Push the new `-SNAPSHOT` version to `master` of the blessed directory

        ```shell
        $ sh droolsjbpbm-build-bootstrap/script/git-all.sh pull --rebase origin master (pulls all changes for master that could be commited in the meantime and prevents merge problems when pushing commits)
        $ sh droolsjbpm-build-bootstrap/script/git-all.sh push origin master (pushes all commits to master)
        ```


    * Switch back to the *release branch name* with `script/git-checkout-all.sh` with drools and jbpm *release branch name*:

        ```shell
        $ sh droolsjbpm-build-bootstrap/script/git-checkout-all.sh 6.2.x 6.2.x
        ```

* Push the created release branches to the blessed directory

    ```shell
    $ sh droolsjbm-build-bootstrap/script/git-all.sh push origin 6.2.x
    ```

* Set up Jenkins build jobs for the branch.

    * Go to the internal Jenkins website inside the VPN.

    * Clone each of the master build jobs for every git repo that was branched.

        * Suffix the build job name with the branch name, for example `drools-6.2.x` and `droolsjbpm-integration-6.2.x`.

        * Change the build job configuration to use the git repo branch, for example `6.2.x`.

* Set up a new Jenkins view for the related release builds

    * https://jenkins.mw.lab.eng.bos.redhat.com/hudson/me/my-views/view/All/

        * Note: Add all Drools, jBPM and Guvnor jobs manually or use a regex pattern similar to `^((drools|guvnor).*5\.5|jbpm.*5\.4).*$`

* Alert the dev mailing list and the IRC channel that the branch has been made.

    * Remind everyone clearly that every new commit to `master` will not make the upcoming CR and Final release, unless they cherry-pick it to this new branch.


#### NOTE:
* at this point we have created a release branch
* we have updated the master branch to the new development version (`*-SNAPSHOT`)
* we have pushed the created release branches to origin
* we have set up a new Jenkins view for the created "release branch"


Releasing from a release branch
-------------------------------

* Alert the IRC dev channels that you're starting the release.

* Pull the latest changes of the branch that will be the base for the release (branchName == master or i.e. 6.2.x)

    ```shell
    $ git-all.sh checkout <branchName>
    $ git-all.sh pull --rebase
    ```

* Create a local release branch

    Name should begin with r, i.e if the release will be 6.2.0.Final the name should be r6.2.0.Final (localReleaseBranchName == r6.2.0.Final)

    ```shell
    $ git-all.sh checkout -b <localReleaseBranchName> <branchname>    
    ```

* Check if everything builds after the last pull & execute all unit tests

    ```shell
    $ mvn-all.sh clean install -Dfull -Dmaven.test.failure.ignore=true > testResult.txt
    # This will execute the build and execute the unit tests and write all logs into testResult.txt.
    ```

* Explore testResult.txt to see if the build breaks or which unit tests are failing.

* Mail to leads of projects the failed unit tests.

* Do another sanity check.

If everything is perfect (compiles, Jenkins is all blue, sanity checks succeed and there is nothing to do about the failed unit tests):

* Define the version and adjust the sources accordingly:

    * First define the version.

        * There are only 4 acceptable patterns:

            * `major.minor.micro.Alpha[n]`, for example `1.2.3.Alpha1`

            * `major.minor.micro.Beta[n]`, for example `1.2.3.Beta1`

            * `major.minor.micro.CR[n]`, for example `1.2.3.CR1`

            * `major.minor.micro.Final`, for example `1.2.3.Final`

        * See the [JBoss version conventions](http://community.jboss.org/wiki/JBossProjectVersioning)

            * Not following those, for example `1.2.3` or `1.2.3.M1` results in OSGi eclipse updatesite corruption.

        * **The version has 3 numbers and qualifier. The qualifier is case-sensitive and starts with a capital.**

            * Use the exact same version everywhere (especially in URL's).

    * Adjust the version in the poms, manifests and other eclipse stuff.

            $ droolsjbpm-build-bootstrap/script/release/update-version-all.sh 6.2.0-SNAPSHOT 6.2.0.Final

        * Note: the arguments are `releaseOldVersion releaseNewVersion`

        * WARNING: FIXME the update-version-all script does not work correctly if you are releasing a hotfix version.

        * WARNING: Guvnor has a hard-coded version number in org.drools.guvnor.server.test.GuvnorIntegrationTest.createDeployment. This must be changed manually and committed.

        * WARNING: script update-version-all.sh did not update automatically all versions in all modules. Check all have been updated with the following and re-run if required.

            ```shell
            $ grep -r '6.2.0-SNAPSHOT' **/pom.xml
            # or
            for i in $(find . -name "pom.xml"); do grep '6.2.0-SNAPSHOT' $i; done
            ```
            OR
            ```shell
            $ grep -ER --exclude-dir=*git* --exclude-dir=*target* --exclude-dir=*idea* --exclude=*ipr --exclude=*iws --exclude=*iml --exclude=workspace* --exclude-dir=*.errai 6.3.0-SNAPSHOT . | grep -v ./kie-wb-distributions/kie-eap-integration/kie-eap-modules/kie-jboss-eap-base-modules.
            ```

    * versions that have to be changed manually

        NOTE: in droolsjbpm-build-bootstrap pom.xml there are some properties where you should pay attention to:

        1. jboss-ip bom version (https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/pom.xml#L11)
           the version of jboss-integration-platform-bom. should be the most recent version released  in jboss-ip-bom

        2. org.kie version (https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/pom.xml#L48)
           org.kie version sometimes has to be changed manually, if needed, should be updated to release version

        3. uberfire version (https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/pom.xml#L54)
           has to be updated manually to the last released version

        4. dashbuilder version (https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/pom.xml#L55)
           has to be updated manually to the last released version

        5. jboss-ip-bom version (https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/pom.xml#L66)
           should be the same version as in point 1

        6. latest released version (https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/pom.xml#L85)
           this is a property productisation needs to get the last released version on the branch where released from.
           When updated this should be pushed to the branch of the blessed repository

        7. latest released *Final* version, property called `version.org.kie.latestFinal.release`
           This property needs to be updated only with *Final* releases (not for Betas, CRs, etc). This property is being
           used to check KIE API backwards compatibility (and there may be other uses as well).

    * Commit those changes (so you can tag them properly):

        * Add changes from untracked files if there are any. WARNING: DO NOT USE `git add .` . You may accidentally add files that are not meant to be added into git.

            ```shell
            $ git add {filename}
            ```

        * Commit all changes

            ```shell
            $ droolsjbpm-build-bootstrap/script/git-all.sh commit -m "Set release version: 6.2.0.Final"
            ```

        * Adjust the property *`<latestReleasedVersionFromThisBranch>`* in *droolsjbpm-build-bootstrap/pom.xml*

         This should be the version that will be released now.
         This is important as productisation takes this version to define theirs.

         * Add this change
         * Commit this change.


        
* Push release branches to github repository

    The release branches rX.X.X.Y should be pushed to the github repository (community=kiegroup/... or product=jboss-integration/...), so the branch
    is available for all future steps. People can access it to review, if all commits that should be in the release were commited.<br>
    This branch has to be removed when doing the next release as a new branch starting with "r" will be pushed and we want prevent having a bunch of "obsolete" release branches.


* Create the tag locally. The arguments are the Drools version, the jBPM version:

    ```shell
    $ droolsjbpm-build-bootstrap/script/release/git-tag-locally-all.sh 6.2.0.Final 6.2.0.Final
    ```


* Go to [nexus](https://repository.jboss.org/nexus), menu item *Staging repositories*, drop all your old staging repositories.


* Deploy the artifacts:

    ```shell
    $ droolsjbpm-build-bootstrap/script/mvn-all.sh clean deploy -Dfull -DskipTests
    ```

    * This will take a long while (3+ hours)

    * The release skips the tests because jbpm and guvnor have random failing tests

    * If it fails for any reason, go to nexus and drop your stating repositories again and start over.

* Go to [nexus](https://repository.jboss.org/nexus), menu item *Staging repositories*, find your staging repository.

    * Look at the files in the repository.

        * Sometimes they are split into 2 staging repositories (with no intersecting files): just threat those 2 as 1 staging repository.

    * Button *close*

        * This will validate the nexus rules. If any fail: fix the issues, and force a git retag locally.


* Do another sanity check of the artifacts by running the examples and opening the manuals from the zips. See above.


* This is **the point of no return**.

    * Warning: The slightest change after this requires the use of the next version number!

        * **NEVER TAG OR DEPLOY A VERSION THAT ALREADY EXISTS AS A PUSHED TAG OR A DEPLOY!!!**

            * Except deploying `SNAPSHOT` versions.

            * Git tags are cached on developer machines forever and are never refreshed.

            * Maven non-snapshot versions are cached on developer machines and proxies forever and are never refreshed.

        * So even if the release is broken, do not reuse the same version number! Create a hotfix version.


* Define the next development version an adjust the sources accordingly:

    * Checkout to the master-branch or the branch which is the base for this release.

        ```shell
        $ git-all.sh checkout master (or base release branch i.e. 6.2.x)
        ```

    * Define the next development version on the branch from which you are releasing.

        * There are only 1 acceptable pattern:

            * `major.minor.micro-SNAPSHOT`, for example `1.2.0-SNAPSHOT` or `1.2.1-SNAPSHOT`

        * Warning: The release branch should never have the same SNAPSHOT version as any other branch.

            * If you're releasing a Final, increment the micro number, not the minor number.

    * Adjust the version in the poms, manifests and other eclipse stuff:

        ```shell
        $ droolsjbpm-build-bootstrap/script/release/update-version-all.sh 6.2.0.Final 6.3.0-SNAPSHOT 6.2.0.Final 6.3.0-SNAPSHOT
        ```

        * Commit those changes:

            ```shell
            $ droolsjbpm-build-bootstrap/script/git-all.sh add .

            $ droolsjbpm-build-bootstrap/script/git-all.sh commit -m "Set next development version: 6.3.0-SNAPSHOT"
            ```

        * Push all changes to the blessed repository:

            ```shell
            $ droolsjbpm-build-bootstrap/script/git-all.sh push
            ```

        * Warning: Guvnor has a hard-coded version number in org.drools.guvnor.server.test.GuvnorIntegrationTest.createDeployment. This must be changed manually and committed.

        * Warning: script update-version-all.sh did not update all versions in all modules for 6.2.0.Final. Check all have been updated with the following and re-run if required.

            ```shell
            $ grep -r '6.2.0-SNAPSHOT' **/pom.xml
            # or
            for i in $(find . -name "pom.xml"); do grep '6.2.0-SNAPSHOT' $i; done
            ```

        * Warning: If releasing from master (i.e. a Beta release) and the push fails as there have been other commits to the remote master branch it might be necessary to pull.

            ```shell
            $ droolsjbpm-build-bootstrap/script/git-all.sh pull
            ```

    * Checkout back to your local release branch.

        ```shell
        $ git-all.sh checkout r6.2.0.Final
        ```


* Push the local tag from the local release branch to the remote blessed repository.

      $ droolsjbpm-build-bootstrap/script/release/git-push-tag-all.sh 6.2.0.Final 6.2.0.Final

    * Push your changes to the release branch:

        * Especially if the release branch is master: First pull any latest changes **without `--rebase`**, .

            ```shell
            $ git-all.sh pull
            ```

            * Without the `--rebase` it's a merge, and their commits will not be rebased before your version-changing commits.

        * Push your version-changing commits to the release branch:

            ```shell
            $ git-all.sh push origin 5.2.x
            ```

* Release your staging repository on [Nexus](https://repository.jboss.org/nexus)

    * Button *release*

* Go to [JIRA](https://issues.jboss.org) and for each of our JIRA projects (DROOLS, PLANNER, JBPM, GUVNOR):

    * Open menu item *Administration*, link *Manage versions*, release the version.

    * Create a new version if needed. There should be at least 2 unreleased non-FUTURE versions.

* Upload the zips, documentation and javadocs to filemgmt and update the website.

    * Go to `kie-wb-distributions/droolsjbpm-uber-distribution/target`.

    * To get access to `filemgmt.jboss.org`, see preparation above.

    * Folder `download_jboss_org` should be uploaded to `filemgmt.jboss.org/downloads_htdocs/drools/release`
    which ends up at [download.jboss.org](http://download.jboss.org/drools/release/)

        * Update [the download webpage](http://www.jboss.org/drools/downloads) accordingly.

    * Folder `docs_jboss_org` should be uploaded to `filemgmt.jboss.org/docs_htdocs/drools/release`
    which ends up at [docs.jboss.org](http://download.docs.org/drools/release/)

        * Use `documentation_table.txt` to update [the documentation webpage](http://www.jboss.org/drools/documentation).

* Update the symbolic links `latest` and `latestFinal` links on filemgmt, if and only if there is no higher major or minor release was already released.

    ```shell
    $ droolsjbpm-build-bootstrap/script/release/create_filemgmt_links.sh 5.2.0.Final
    ```

    * Wait 5 minutes and then check these URL's. Hit ctrl-F5 in your browser to do a hard refresh:

        * [http://download.jboss.org/drools/release/latest/](http://download.jboss.org/drools/release/latest/)

        * [http://download.jboss.org/drools/release/latestFinal/](http://download.jboss.org/drools/release/latestFinal/)

        * [http://docs.jboss.org/drools/release/latest/](http://docs.jboss.org/drools/release/latest/)

        * [http://docs.jboss.org/drools/release/latestFinal/](http://docs.jboss.org/drools/release/latestFinal/)

* If it's a Final, non-hotfix release: publish the XSD file(s), by copying each XSD file to its website.

    * The Drools XSD files are at http://www.drools.org/xsd/[http://www.drools.org/xsd/]
    
    * Go to the https://github.com/kiegroup/droolsjbpm-knowledge/blob/master/kie-api/src/main/resources/org/kie/api/kmodule.xsd[kmodule.xsd] file (on master) and switch to the release tag.
    
    * Copy the raw file to https://github.com/kiegroup/drools-website/tree/master/xsd[drools-website's `xsd` directory].
    
    * Rename it from `kmodule.xsd` to `kmodule_<major>_<minor>.xsd` so it includes its version (major and minor only, not hotfixes or quantifiers). For example for release `6.3.0.Final` it is renamed to `kmodule_6_3.xsd`. Do not overwrite an existing file as there should never be an existing file (because the XSD is only copied for Final, non-hotfix releases).
    
    * Publish drools.org

Announcing the release
----------------------

* Create a blog entry on [the kiegroup blog](http://blog.athico.com/)

    * Include a direct link to the new and noteworthy section and to that blog entry in all other correspondence.

    * Twitter and Google+ the links.

        * Most people just want to read the new and noteworthy, so link that first.

    * Mail the links to the user list.

* If it's a Final, non-hotfix release:

    * Notify TheServerSide and Dzone's Daily Dose.


Building a Product Tag
======================
**This paragraph describes the building of a product tag when the version is > = 6.2.x!
(for version == 6.0.x please look at the next paragraph Synching the Product Repository)**

The community code repositories under the @kiegroup account contains all the code released as part of the community projects for Drools and jBPM. Every time a new minor or major version is released,
a new community branch is created for that version. For instance, at the time of this writing, we have, for instance, branches *6.0.x*, *5.6.x*, *5.5.x*, etc for each minor/major version released and
the *master* branch for future releases. Red Hat also has a mirror private repository that is used as a base for the product releases. This mirror repository contains all the code from the community
repositories, plus a few product specific commits, comprising branding commits (changing names, for instance from Drools to BRMS), different icons/images, etc.

This new tag will usually be based on the HEAD of a specific community branch with the product specific commits applied on top of it.

Follows an instruction on how to do that. These instructions assume:

* You have a local clone of all Drools/jBPM repositories (18 at the time of this writing).
* The clones have a remote repository reference to the @kiegroup repositories that we will name **main**
* The clones have a remote repository reference to the @jboss-integration mirrors of these repositories that we will name **product**

Here are the steps:

**1 - cd into the scripts directory**

    $ cd droolsjbpm-build-bootstrap/script

**2 - Fetch the changes from the _main_ repository:**

    $ ./git-all.sh fetch main

**3 - Rebase the corresponding branches (master and 6.2.x at the time of this writing)**

    $ ./git-all.sh rebase main/master master
    $ ./git-all.sh rebase main/6.2.x 6.2.x

**4 - Create a local branch to base the tag on. I usually name the base branch as "bsync.YYYY.MM.DD" where YYYY.MM.DD is the year, month and day when the tag is being created.**

    $ ./git-all.sh checkout -b bsync.YYYY.MM.DD <branch to base the tag on>

**5 - Build local branch with product specific commits to make sure it is working. Fix any problems in case it is not working.**

    $ ./mvn-all.sh clean install -Dfull -Dcustom-container -DskipTests -Dproductized

**6 - Create the tag for all repositories. For product tags, we use a naming standard of "sync.YYYY.MM.DD", where YYYY.MM.DD is the date the tag is created. If for any reason more than one tag needs to be created on the same day, add a sequential counter sufix: "sync.YYYY.MM.DD.C"**

    $ ./git-all.sh tag sync.YYYY.MM.DD

**7 - Push the tag and branches to the _product_ server.**

    $ ./git-all.sh push product sync.YYYY.MM.DD
    $ ./git-all.sh push product 6.2.x
    $ ./git-all.sh push product master


Syncing the Product Repository
===============================

**Note: This is only for 6.0.x versions!**

**1 - cd into the scripts directory**

    $ cd droolsjbpm-build-bootstrap/script

**2 - Fetch the changes from the _main_ repository:**

	$ ./git-all.sh fetch main

**3 - Rebase the corresponding branches (master and 6.0.x at the time of this writing, and 0.3.x branch for Uberfire)**

    $ ./git-all.sh rebase main/master master
    $ ./git-all.sh rebase main/6.0.x 6.0.x

The second command above will raise an error in the Uberfire repository as the branch in Uberfire is named 0.3.x. Ignore the error and in another shell, cd into the uberfire folder and manually rebase Uberfire:

    $ cd <uberfire clone directory>
    $ git rebase main/0.3.x 0.3.x

**4 - Fetch the changes from the _prod_ repository:**

    $ ./git-all.sh fetch prod

At the time of this writing, there are only 4 repositories that contain product specific branches. The fetch should only return changes, if it returns, in those 4 repositories. In case any change is picked up in any other repository or in any branch that is not the product branch, someone made a mistake and committed changes to the product repository. This has to be fixed. The 4 repositories are:

* jbpm-console-ng
* dashboard-builder
* jbpm-dashboard
* kie-wb-distribution

**5 - For each of the 4 repositories, in another shell, rebase the product branch:**

    $ cd <repository>
    $ git rebase prod/prod-6.0.1.GA.x-2014.02.10 prod-6.0.1.GA.x-2014.02.10

Please note that the above has to be done for each repository that contains product specific branches. Please also note that the product branch name might be different. The example above uses the branch name at the time of this writing.

**6 - Checkout the branch that will serve as the base for the tag on all repositories. This might be a release branch in case the tag will be created based on a community release, or it can be a regular branch like 6.0.x (0.3.x in case of Uberfire):**

    $ ./git-all.sh checkout 6.0.x

The above will raise an error for Uberfire, so in another shell do:

    $ cd <uberfire folder>
    $ git checkout 0.3.x

**7 - Create a branch to base the tag on. I usually name the base branch as "bsync.YYYY.MM.DD" where YYYY.MM.DD is the year, month and day when the tag is being created.**

    $ ./git-all.sh checkout -b bsync.2014.10.12

**8 - For each repository with a product specific branch, it is necessary to rebase the product branch on top of the base code. There are several different ways to do that. I prefer to reset the tag branch to the product branch and then rebase it. Here are the steps to do that. In another shell, cd into the repository that contains the product branch, reset the current release branch to the product branch, rebase it on top of the base branch.**

    $ cd <repository folder>
    $ git reset --hard prod-6.0.1.GA.x-2014.02.10
    $ git rebase 6.0.x

Please note that the example above uses the same branch names used in setp (5) for product branch and (6) for the base branch.
If the rebase creates any conflicts, fix the conflicts and continue the rebase.

**9 - If any conflict happened in step 8, then we need to create new product branches. For each repository with a product branch, cd into the repository folder, create a new product branch and checkout the tag branch again.**

    $ cd <repository folder>
    $ git checkout -b prod-6.0.1.GA.x-2014.02.12
    $ git checkout bsync.2014.10.12

**10 - If there are any commits that have to be manually cherry-picked into the tag, cd into the corresponding repository and cherry-pick the commit. This should not happen often, but sometimes it does.**

    $ cd <repository>
    $ git cherry-pick -x <SHA>

**11 - Build the code for all repositories and test to make sure it is working. Fix any problems in case it is not working.**

**12 - Create the tag for all repositories. For product tags, we use a naming standard of "sync.YYYY.MM.DD", where YYYY.MM.DD is the date the tag is created. If for any reason more than one tag needs to be created on the same day, add a sequential counter sufix: "sync.YYYY.MM.DD.C"**

    $ ./git-all.sh tag sync.2014.02.12

**13 - Push the tag and branches to the _prod_ server.**

    $ ./git-all.sh push prod sync.2014.02.12
    $ ./git-all.sh push prod 6.0.x
    $ ./git-all.sh push prod master

**14 - In case a new product branch was created in step 9, push the new product branch and delete the old remote branch:**

    $ git push prod-6.0.1.GA.x-2014.02.12
    $ git push :prod-6.0.1.GA.x-2014.02.10

Please note that this will not delete the old local product branch. I usually leave the local branch around for a few weeks just in case some mistake happened, as it will make it easier to fix, but it can be deleted.
