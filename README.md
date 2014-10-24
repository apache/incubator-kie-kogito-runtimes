Developing Drools and jBPM
==========================

**If you want to build or contribute to a droolsjbpm project, read this document.**

**This document will save you and us a lot of time by setting up your development environment correctly.**
It solves all known pitfalls that can disrupt your development.
It also describes all guidelines, tips and tricks.
If you want your pull requests (or patches) to be merged into master, please respect those guidelines.

If you are reading this document with a normal text editor, please take a look
at the more readable [formatted version](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/README.md).

If you discover pitfalls, tips and tricks not described in this document,
please update it using the [markdown syntax](http://daringfireball.net/projects/markdown/syntax).

Table of content
----------------

* **[Source control with Git](#source-control-with-git)**

* **[Building with Maven](#building-with-maven)**

* **[Developing with Eclipse](#developing-with-eclipse)**

* **[Developing with IntelliJ](#developing-with-intellij)**

* **[Team communication](#team-communication)**

* **[Writing documentation](#writing-documentation)**

* **[Releasing](#releasing)**

* **[Synching the Product Repository](#synching-the-product-repository)**

* **[FAQ](#faq)**

Quick start
===========

If you don't want to contribute to this project and you know git and maven, these build instructions should suffice:

* To build 1 repository, for example `guvnor`:

        $ git clone git@github.com:droolsjbpm/guvnor.git
        $ cd guvnor
        $ mvn clean install -DskipTests

* To build all repositories:

        $ git clone git@github.com:droolsjbpm/droolsjbpm-build-bootstrap.git
        $ droolsjbpm-build-bootstrap/script/git-clone-others.sh
        $ droolsjbpm-build-bootstrap/script/mvn-all.sh clean install -DskipTests

**If you want to contribute to this project, read the rest this file!**

Source control with Git
=======================

Installing and configuring git
------------------------------

* Install it in your OS:

    * Linux: Install the package git

            $ sudo apt-get install git

        Tip: Also install *gitk* to visualize your git log:

            $ sudo apt-get install gitk

    * Windows, Mac OSX: Download from [the git website](http://git-scm.com).

        Tip for Mac OSX: Also install [*gitx*](http://gitx.frim.nl/) to visualize your git log.

    * More info [in github's git installation instructions](http://help.github.com/git-installation-redirect).

* Check if git is installed correctly.

        $ git --version
        git version 1.7.1

* Configure git correctly:

        $ git config --global user.name "My Full Name"
        $ git config --global user.email myAccount@gmail.com
        $ git config --global -l
        user.name=Geoffrey De Smet
        user.email=gds...@gmail.com

    * Warning: the field `user.name` is your full name, *not your username*.

    * Note: the field `user.email` should match an email address of your github account.

    * More info on [github](http://help.github.com/git-email-settings/).

* Get a github account

    * And add your public key on github: [Follow these instructions](http://github.com/guides/providing-your-ssh-key).

* To learn more about git, read the free book [Git Pro](http://progit.org/book/).

Getting the sources locally
---------------------------

Because you 'll probably want to change our code, it's recommended to fork our code before cloning it,
so it's easier to share your changes with us later.
For more info on forking, read [Github's help on forking](http://help.github.com/fork-a-repo/).

* First fork the repository you want to work on, for example `guvnor`:

    * Surf to [the blessed repositories on github](https://github.com/droolsjbpm) and log in.

        * Note: **Every git repository can be build alone.**
        You only need to fork/clone the repositories you're interested in (`guvnor` in this case).

    * Surf to [the specific repository (`guvnor`)](https://github.com/droolsjbpm/guvnor)

    * Click the top right button *Fork*

    * Note: by forking the repository, you can commit and push your changes without our consent
    and we can easily review and then merge your changes into the blessed repository.

* **Clone your fork locally:**

        # First make a directory to hold all the droolsjbpm projects
        $ mkdir droolsjbpm
        $ cd droolsjbpm

        # Then clone the repository you want to clone.
        $ git clone git@github.com:MY_GITHUB_USERNAME/guvnor.git
        $ cd guvnor
        $ ls

    * Warning: Always clone with the *SSH URL*, never clone with the *HTTPS URL* because the latter is unreliable.

    * Note: it's highly recommended to name the cloned directory the same as the repository (which is the default), so the helper scripts work.

    * By default you will be looking at the sources of the master branch, which can be very unstable.

        * Use git checkout to switch to a more stable branch or tag:

                $ git checkout 5.2.0.Final

* Add the blessed repository as upstream (if you've directly cloned the blessed repository, don't do this):

        $ git remote add upstream git@github.com:droolsjbpm/guvnor.git
        $ git fetch upstream

Working with git
----------------

* First make a topic branch:

        $ git checkout master
        $ git checkout -b myFirstTopic

    * Don't litter your local `master` branch: keep it equal to `remotes/upstream/master`

    * 1 branch can have only 1 pull request, because the pull requests evolves as you add more commits on that branch.

* Make changes, run, test and document them, then commit them:

        $ git commit -m"Fix typo in documentation"

* Push those commits on your topic branch to your fork

        $ git push origin myFirstTopic

* Get the latest changes from the blessed repository

    * Set your master equal to the blessed master:

            $ git fetch upstream
            $ git checkout master
            # Warning: this deletes all changes/commits on your local master branch, but you shouldn't have any!
            $ git reset --hard upstream/master

    * Start a new topic branch and set the code the same as the blessed master:

        $ git fetch upstream && git checkout -b mySecondTopic && git reset --hard upstream/master

    * If you have a long-running topic branch, merge master into it:

            $ git fetch upstream
            $ git merge upstream/master

        * If there are merge conflicts:

                $ git mergetool
                $ git commit

            or

                $ git status
                $ gedit conflicted-file.txt
                $ git add conflicted-file.txt
                $ git commit

            Many people get confused when a merge conflict occurs, because you're *in limbo*.
            Just fix the merge conflicts and commit (even if the git seems to contain many files),
            only then is the merge over. Then run `git log` to see what happened.
            The many files in the merge conflict resolving commit are a side-affect of non-linear history.

* You may delete your topic branch after your pull request is closed (first one deletes remotely, second one locally):

        $ git push origin :myTopicBranch
        $ git branch -D myTopicBranch

* Tips and tricks

    * To see the details of your local, unpushed commits:

            $ git diff origin...HEAD

    * To run a git command (except clone) over all repositories (only works if you cloned all repositories):

            $ cd ~/projects/droolsjbpm
            $ droolsjbpm-build-bootstrap/script/git-all.sh push

        * Note: the `git-all.sh` script is working directory independent.

        * Linux tip: Create a symbolic link to the `git-all.sh` script and place it in your `PATH` by linking it in `~/bin`:

                $ ln -s ~/projects/droolsjbpm/droolsjbpm-build-bootstrap/script/git-all.sh ~/bin/droolsjbpm-git

            For command line completion, add the following line in `~/.bashrc`:

                $ complete -o bashdefault -o default -o nospace -F _git droolsjbpm-git

Share your changes with a pull request
--------------------------------------

A pull request is like a patch file, but easier to apply, more powerful and you'll be credited as the author.

* Creating a pull request

    * Push all your commits to a topic branch on your fork on github (if you haven't already).

        * You can only have 1 pull request per branch, so it's advisable to use topic branches to avoid mixing your changes.

    * Surf to that topic branch on your fork on github.

    * Click the button *Pull Request* on the top of the page.

* Accepting a pull request

    * Surf to the pull request page on github.

    * Review the changes

    * Click the button *Merge help* on the bottom of the page and follow the instructions of github to apply those changes on the blessed master.

        * Or use the button *Merge* if there are no merge conflicts.

Building with Maven
===================

All projects use Maven 3 to build all their modules.

Installing Maven
----------------

* Get Maven

    * [Download Maven](http://maven.apache.org/) and follow the installation instructions.

* Linux

    * Note: the `apt-get` version of maven is probably not up-to-date enough.

    * Linux trick to easily upgrade to future versions later:

        * Unzip maven to `~/opt/build`
    
        * Create a version-independent link:

                $ cd ~/opt/build/
                $ ln -s apache-maven-3.0.3 apache-maven

            Next time you only have to remove the link and recreate the link to the new version.

        * Add this to your `~/.bashrc` file:

                export M3_HOME="~/opt/build/apache-maven"
                export PATH="$M3_HOME/bin:$PATH"

    * Give more memory to maven, so it can the big projects too:

        * Add this to your `~/.bashrc` file:

            export MAVEN_OPTS="-Xms256m -Xmx1024m -XX:MaxPermSize=512m"

* Windows:

    * Give more memory to maven, so it can the big projects too:

        * Open menu *Configuration screen*, menu item *System*, tab *Advanced*, button *environment variables*:

            set MAVEN_OPTS="-Xms256m -Xmx1024m -XX:MaxPermSize=512m"

* Check if maven is installed correctly.

        $ mvn --version
        Apache Maven 3.0.3 (...)
        Java version: 1.6.0_24

    Note: the enforcer plugin enforces a minimum maven and java version.

Running the build
-----------------

* Go into a project's base directory, for example `guvnor`:

        $ cd ~/projects/droolsjbpm
        $ ls
        drools  droolsjbpm-build-bootstrap  droolsjbpm-build-distribution  droolsjbpm-integration  droolsjbpm-knowledge  droolsjbpm-tools  optaplanner  guvnor
        $ cd guvnor
        $ ls
        ...  guvnor-repository  guvnor-webapp-drools  pom.xml

    Notice you see a `pom.xml` file there. Those `pom.xml` files are the heart of Maven.

* **Run the build**:

        $ mvn clean install -DskipTests

    The first build will take a long time, because a lot of dependencies will be downloaded (and cached locally).

    It might even fail, if certain servers are offline or experience hiccups.
    In that case, you 'll see an IO error, so just run the build again.

    If you consistently get `Could not transfer artifact ... Connection timed out`
    and you are behind a non-transparent proxy server,
    [configure your proxy server in Maven](http://maven.apache.org/settings.html#Proxies).

    After the first successful build, any next build should be fast and stable.

* Try running a different profile by using the option `-D<profileActivationProperty>`:

        $ mvn clean install -DskipTests -Dfull

    There are 3 profile activation properties:

    * *none*: Fast, for during development

    * `full`: Slow, but builds everything (including documentation). Used by jenkins and during releases.

    * `productized`: activates branding changes for productized version

* To run a maven build over all repositories (only works if you cloned all repositories):

        $ cd ~/projects/droolsjbpm
        $ droolsjbpm-build-bootstrap/script/mvn-all.sh -DskipTests clean install

    * Note: the `mvn-all.sh` script is working directory independent.

* Warning: The first `mvn` build of a day will download the latest SNAPSHOT dependencies of other droolsjbpm projects,
unless you build all those droolsjbpm projects from source.
Those SNAPSHOTS were build and deployed last night by jenkins jobs.

    * If you 've pulled all changes (or cloned a repository) today, this is a good thing:
    it saves you from having to download and build all those other latest droolsjbpm projects from source.

    * If you haven't pulled all changes today, this is probably a bad thing:
    you 're probably not ready to deal with those new snapshots.

        In that case, add `-nsu` (= `--no-snapshot-updates`) to the `mvn` command to avoid downloading those snapshots:

            $ mvn clean install -DskipTests -nsu

        Note that using `-nsu` will also make the build faster.

Running tests
-------------

Guvnor uses Arquillian to run tests in a J2EE container and hence tests need to be ran differently to others.

* Guvnor

        $ cd ~/projects/droolsjbpm/guvnor/guvnor-webapp-drools
        $ mvn integration-test [-Dtest=ATestClassName]

* All other modules

        $ cd ~/projects/droolsjbpm/drools
        $ mvn test [-Dtest=ATestClassName]

Configuring Maven
-----------------

To deploy snapshots and releases to nexus, you need to add this to the file `~/.m2/settings.xml`:

     <settings>
       ...
       <servers>
         <server>
           <id>jboss-snapshots-repository</id>
           <username>jboss.org_username</username>
           <password>jboss.org_password</password>
         </server>
         <server>
           <id>jboss-releases-repository</id>
           <username>jboss.org_username</username>
           <password>jboss.org_password</password>
         </server>
       </servers>
       ...
     </settings>

Furthermore, you'll need nexus rights to be able to do this.

More info in [the JBoss.org guide to get started with Maven](http://community.jboss.org/wiki/MavenGettingStarted-Developers).

Requirements for dependencies
-----------------------------

Any dependency used in any KIE project must fulfill these hard requirements:

* The dependency must have **an ASL compatible license**.

    * Good: BSD, MIT, ASL

    * Avoid: EPL, LGPL

        * Especially LGPL is a last resort and should be abstracted away or contained behind an SPI.
        
        * Test scope dependencies pose no problem if they are EPL or LPGL.

    * Forbidden: no license, GPL, AGPL, proprietary license, field of use restrictions ("this software shall be used for good, not evil"), ...
    
        * Even test scope dependencies cannot use these licenses.

* The dependency shall be **available in [Maven Central](http://search.maven.org/) or [JBoss Nexus](https://repository.jboss.org/nexus)**.

    * Any version used must be in the repository Maven Central and/or JBoss (Nexus) Public repository group

        * Never add a `<repository>` element in a `pom.xml`.

        * Note: JBoss Public repository group mirrors java.net, codehaus.org, ... Most jars are available there.

    * Why?

        * Build reproducibility. Any repository server we use, must still run 7 years from now.

        * Build speed. More repositories slow down the build.

        * Build reliability. A repository server that is temporary down can break builds.

    * Workaround to still use a great looking jar as a dependency:

        * Get that dependency into JBoss Nexus as a 3rd party library.

* The dependency must be able to run on any **JVM 1.6 and higher**.

    * It must be compiled for Java target 1.6 or lower (even if it's compiled with JDK 7 or JDK 8).

    * It must not use any JDK API's that were not yet available in Java 1.6.

* **Do not release the dependency yourself** (by building it from source).

    * Why? Because it's not an official release, by the official release guys.

        * A release must be 100% reproducible.

        * A release must be reliable (sometimes the release person does specific things you might not reproduce).

* **No security issues** (CVE's) reported on that version of the dependency

    * We don't expect you to check this manually:
    The victims enforcer plugin will automatically fail the build if a known bad dependency is used.

Any dependency used in any KIE project should fulfill these soft requirements:

* Use dependencies that are **acceptable for the [jboss-integration-platform-bom](https://github.com/jboss-integration/jboss-integration-platform-bom)**.

    * Do not override versions in `kie-parent-with-dependencies`'s `pom.xml` unless an exception is granted

        * If a newer version of the ip-bom already uses the new version, it's of course fine to do a temporarly overwrite in `kie-parent-with-dependencies`'s `pom.xml`.

* **Prefer dependencies with the groupId `org.jboss.spec`** over those with the groupId `javax.*`.

    * Dependencies with the groupId `javax.*` are unreliable and are missing metadata. No one owns/maintains them consistently.

    * Dependencies with the groupId `org.jboss.spec` are checked and fixed by JBoss.

* Only use dependencies with **an active community**.

    * Check for activity in the last year through [Ohloh](http://www.ohloh.net).

* Less is more: **less dependencies is better**. Bloat is bad.

    * Try to use existing dependencies if the functionality is available in those dependencies

        * For example: use `poi` instead of `jexcelapi` if `poi` is already a KIE dependency

* **Do not use fat jars, nor shading jars.**

    * A fat jar is a jar that includes another jar's content. For example: `weld-se.jar` which includes `org/slf4j/Logger.class`

    * A shaded jar is a fat jar that shades that other jar's content. For example: `weld-se.jar` which includes `org/weld/org/slf4j/Logger.class`

    * Both are bad because they cause dependency tree trouble. Use the non-fat jar instead, for example: `weld-se-core.jar`

There are currently a few dependencies which violate some of these rules.
If you want to add a dependency that violates any of the rules above, get approval from the project leads.

Regenerating Protobuf Files
---------------------------

Some modules include Protobuf files (like drools-core and jbpm-flow). Every time a .proto file is changed, the java files have to be regenerated. In order to do that, on the module that contains the files to be regenerated, execute the following command:

        $ mvn exec:exec -Dproto
        
After testing the regenerated files, don't forget to commit them.

**IMPORTANT:** before trying to regenerate the protobuf java files, you must install the protobuf compiler (protoc) in your machine. Please follow the instructions. You can download it from here: https://developers.google.com/protocol-buffers/docs/downloads

For Linux/Mac, you have to compile it yourself as there are no binaries available. Follow the instructions in the README file for that.

Developing with Eclipse
=======================

Before running Eclipse
----------------------

* Do not use an Eclipse version older than `3.6 (helios)`.

* Avoid an `OutOfMemoryException` and a `StackOverflowError` when building.

    Open `$ECLIPSE_HOME/eclipse.ini` and add/change this: on openFile -vmargs:

        openFile
        -vmargs
        ...
        -XX:MaxPermSize=512m
        -Xms512m
        -Xmx1024m
        -Xss1024k

Configuring the project with the m2eclipse plugin
-------------------------------------------------

The m2eclipse plugin is a plugin in Eclipse for Maven.
This is the new way (and compatible with tycho).

* Open Eclipse

* Follow [the installation instructions of m2eclipse](http://m2eclipse.sonatype.org/).

    * Follow the link *Installing m2eclipse* at the bottom.

* Click menu *File*, menu item *Import*, tree item *Maven*, tree item *Existing Maven Projects*.

* Click button *Browse*, select a repository directory. For example `~/projects/droolsjbpm/guvnor`.

* Unfold *Advanced*, textfield *Profiles*: `notSoaProfile,fullProfile`.

For more information, see [the m2eclipse book](http://www.sonatype.com/books/m2eclipse-book/reference/)

Configuring the project with the deprecated maven-eclipse-plugin
----------------------------------------------------------------

The maven-eclipse-plugin plugin is a plugin in Maven for Eclipse.
This is the old way (of which the development has stopped).

Run this command to generate `.project` and `.classpath` files:

    $ mvn eclipse:eclipse

* Open Eclipse

* Menu item *Import existing projects*, navigate to the project base directory, select all the projects (= modules) it lists.

Important note: `mvn eclipse:eclipse` does not work for our eclipse plugins because it is not compatible with tycho
(and never will be).

Configuring Eclipse
-------------------

* Force language level 5 (not 6), to fail-fast on implemented interface methods that are annotated with `@Override`.

    * Open menu *Window*, menu item *Preferences*

    * Click tree item *Java*, tree item *Compiler*, section *JDK Compliance*, combobox *Compiler compliance level* should be `1.5`.

* Remove the test resources Java Build Path exclusion filter to ensure JUnit tests ran inside Eclipse can find the necessary resources.

    * Right-click the project

    * Select menu item *Build Path*, sub-menu item *Configure build path...*

    * On the *Sources* tab, scroll down to `<project>\src\test\resources` and expand tree

    * Select `Excluded` and click *Remove*. The filter should show as `(none)` 

* Set the correct file encoding (UTF-8 except for properties files) and end-of-line characters (unix):

    * Open menu *Window*, menu item *Preferences*.

    * Click tree item *General*, tree item *Workspace*

        * Label *Text file encoding*, radiobutton *Other*, combobox `UTF-8`.

        * Label *New text file delimiter*, radiobutton *Other*, combobox `Unix`.

    * Click tree item *XML*, tree item *XML Files*.

        * Combobox *Encoding*: `ISO 10646/Unicode(UTF-8)`.

    * Click tree item *CSS*, tree item *CSS Files*.

        * Combobox *Encoding*: `ISO 10646/Unicode(UTF-8)`.

    * Open tree item *HTML*, tree item *HTML Files*.

        * Combobox *Encoding*: `ISO 10646/Unicode(UTF-8)`.

    * Note: normal i18n properties files must be in `ISO-8859-1` as specified by the java `ResourceBundle` contract.

        * Note on note: GWT i18n properties files override that and must be in `UTF-8` as specified by the GWT contract.

* Set the correct number of spaces when pressing tab:

    * Warning: If you imported the `eclipse-formatter.xml` file, you don't need to set it for Java, but you do need to set it for XML anyway!

    * Open menu *Window*, menu item *Preferences*.

        * If you have project specific settings enabled instead, right click on the project and click the menu item *Properties*.

    * Click tree item *Java*, tree item *Code Style*, tree item *Formatter*.

        * Click button *Edit* of the active profile, tab *Indentation*

        * Combobox *Tab policy*: `spaces only`

        * Textfield *Indentation size*: `4`

        * Textfield *Tab size*: `4`

        * Note: If it is a build-in profile, you 'll need to change its name with the textfield on top.

    * Click tree item *XML*, tree item *XML Files*, tree item *Editor*.

        * Radiobutton *Indent using space*: `on`

        * Textfield *Indentation size*: `2`

    * Click tree item *General*, tree item *Editors*, tree item *Text Editors*.

        * Checkbox *Insert spaces for tabs*: `on`

        * Textfield *Displayed tab width*: `4`

    * Click tree item *CSS Files*, tree item *Editor*.

        * Radiobutton *Indent using space*: `on`

        * Textfield *Indentation size*: `4`

* Set the correct file headers (do not include @author or a meaningless javadoc):

    * Open menu *Window*, menu item *Preferences*.
    
    * Click tree item *Java*, tree item *Code Style*, tree item *Code Templates*.

    * Click tree *Configure generated code and comments*, tree item *Comments*, tree item *types*.

    * Remove the line *@author Your Name*.

        * We do not accept `@author` lines in source files, see FAQ below.

    * Remove the entire javadoc as automatically templated data is meaningless.

* Set the correct license header

    Eclipse JEE Helios currently has no build-in support of license headers, but you can configure it for new files.

    * Open menu *Window*, menu item *Preferences*.

        * If you have project specific settings enabled instead, right click on the project and click the menu item *Properties*.

    * Click tree item *Java*, tree item *Code Style*, tree item *Copy templates*.

    * Click tree item *Comments*, tree item *Files*.

    * Replace the text area with the java multi-line comment version of
    ` droolsjbpm-build-bootstrap/ide-configuration/LICENSE-ASL-2.0-HEADER.txt`:

        /*
         * Copyright 2011 JBoss Inc
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *       http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */

    * Note: Do not start or end with a newline character

    * Note: Do not start with `/**`: it is not a valid javadoc.

    * Update the year (2011) every year.

Extra Eclipse plugins
---------------------

* Enable git support

    * Open menu *Help*, menu item *Install new software*.

    * Click combobox *Update site* `Helios`, tree item *Collaboration*, tree item *Eclipse EGit*.

* GWT plugin

    * [Download and install the Eclipse GWT plugin](http://code.google.com/intl/en/eclipse/docs/getting_started.html)

        * Note: it is recommended to keep your Eclipse GWT plugin version in sync with the GWT version that we use.

    * In *Package Explorer*, right click on the project `guvnor-webapp`, menu item *Properties*.

        * Enable the GWT aspect:

            * Click tree item *Google*, tree item *Web Toolkit Settings...*

            * Checkbox *Use google Web Tookit*: `on`

            * List *Entry Point Modules* should contain `Guvnor - org.drools.guvnor` (and optionally `FastCompiledGuvnor` too).

        * The gwt-dev jar needs to be first on the compilation classpath (the `java.lang.NoSuchFieldError: warningThreshold` problem)

            * Click tree item *Java Build Path*

            * Tab *Libraries*, button *Add Library...*, list item *Google Web Toolkit*, button *Next*, button *Finish*

            * Tab *Order and Export*, select `GWT SDK ...`, button *Top*

    * Verify that you have a web browser configured in Eclipse:

        * Open menu *Window*, menu item *Preferences*.

        * Click tree *General*, tree item *Web Browser*, radiobutton *Use external web browser*.

        * Click button *New...*, textfield *Name* `firefox`, textfield *Location* `/usr/bin/firefox`, textfield *Parameters* `%URL%`, button *OK*.

        * Check the checkbox next to `firefox`.

    * Run GWT in hosted mode

        * Open project context menu *Properties*, Google->Web application->

               * This project has a WAR directory, tick

               * WAR directory, `target/guvnor-webapp-drools-5.4.0-SNAPSHOT` (this will differ for different releases)

               * You will need to have completed a maven install, as explained above to generate the `target/guvnor-webapp-drools-5.4.0-SNAPSHOT` directory

               * Launch and deploy from this directory, tick

        * Open menu *Run*, menu item *Run configurations...*

        * In the list, select *Web Application*, button *new launch configuration*

        * Tab *Main*, Project: `guvnor-webapp-drools`

        * Tab *Main*, Ensure `Main class` is: `com.google.gwt.dev.DevMode`

        * Tab *GWT*, list *Available Modules*: `Guvnor - org.drools.guvnor`

        * Tab *Arguments*, Ensure `Program Arguments` are : `-war <path-to-war-folder> -remoteUI "${gwt_remote_ui_server_port}:${unique_id}" -startupUrl index.jsp -logLevel INFO -codeServerPort 9997 -port 8888 org.drools.guvnor.FastCompiledGuvnor org.drools.guvnor.Guvnor`. 

               * For example: `-war /home/manstis/workspaces/git/droolsjbpm/guvnor/guvnor-webapp-drools/target/guvnor-webapp-drools-5.4.0-SNAPSHOT -remoteUI "${gwt_remote_ui_server_port}:${unique_id}" -startupUrl index.jsp -logLevel INFO -codeServerPort 9997 -port 8888 org.drools.guvnor.FastCompiledGuvnor org.drools.guvnor.Guvnor`

        * Tab *Arguments*, it is recommended to set `VM Arguments` to: `-XX:MaxPermSize=512m -Xms512m -Xmx2048m`. You might be able to try smaller values, but these are known to work.

        * Button *Run*.

    * In your workspace, in the tab *Development Mode*, double click on the `Guvnor` URL.

    * If you encounter a java.lang.NoSuchFieldError: warningThreshold error you need to follow the steps [here](http://code.google.com/p/google-web-toolkit/issues/detail?id=4479), i.e. 

        * Add GWT-SDK to your classpath (even though it is a Maven dependency)

        * On your Java Build Path, Order and Export tab, move GWT-SDK to the top

Eclipse plugin development
--------------------------

* Installing a droolsjbpm eclipse plugin into a fresh Eclipse from a local update site.

    * Follow the intructions in [the description entity in the org.drools.updatesite pom.xml file](https://github.com/droolsjbpm/droolsjbpm-tools/blob/master/drools-eclipse/org.drools.updatesite/pom.xml).

Developing with IntelliJ
========================

Before running IntelliJ
-----------------------

* Avoid an `OutOfMemoryException` while editing or building.

    Open `$IDEA_HOME/bin/idea.vmoptions` and change the first 3 values to this:

        -Xms512m
        -Xmx1024m
        -XX:MaxPermSize=512m

Configuring the project with the maven integration
--------------------------------------------------

IntelliJ has very good build-in support for Maven.

* Open IntelliJ.

* Click menu *File*, menu item *New project*.

    * Click radiobutton *Create project from scratch*, button *Next*

    * Textfield *name*: `droolsjbpm`

    * Textfield *Project files location*: `~/projects/droolsjbpm`

    * Checkbox *Create module*: `off`

Note: If you want to configure a main project that includes all projects you must create an empty project and add the
projects as modules.

* Click menu *File*, menu item *New module*

    * Radiobutton *Import from external model*, button *Next*, button *Next*

    * Textfield *Root directory*: `~/projects/droolsjbpm/guvnor`

        * That is the directory that contains the multiproject `pom.xml` file from a project base directory.

    * Button *Next*, check in the *Selected profiles* `notSoaProfile` and `fullProfile`, button *Next*, button *Finish*.

    * Go grab a coffee while it's indexing.

    * Repeat if you want to work on more than 1 droolsjbpm project.

Note: Don't use the `maven-idea-plugin` on the command line with `mvn`: it's dead.

Configuring IntelliJ
--------------------

* Force language level 5 (not 6), to fail-fast on implemented interface methods that are annotated with `@Override`.

    * Open menu *File*, menu item *Project Structure*

    * Click list item *Modules*, for each module, tab *Sources*, combobox *Language level* should be automatically set to `5.0 ...`

* Avoid that changes in some resources are ignored in the next run/debug (and you are forced to use mvn)

    * Open menu *File*, menu item *Settings*

    * Click tree item *Compiler*, textfield *Resource patterns*: change to `!?*.java` (remove other content)

* Avoid a `StackOverflowError` when building

    * Open menu *File*, menu item *Settings*

    * Click tree item *Compiler*, tree item *Java Compiler*, textfield *Additional command line parameters*

    * Add ` -J-Xss1024k` so it becomes something like `-target 1.5 -J-Xss1024k`

* Include files with non-default extensions in your searches and refactors

    * Open menu *File*, menu item *Settings*

    * Click tree item *File Types*, in the list *Recognized File Types*:

        * Next to list *Recognized File Types*, click on the button *Add...*

            * Textfield *name*: `DRL files`

            * Textfield *Line comment*: `//`

            * Textfield *Block comment start*: `/*`

            * Textfield *Block comment end*: `*/`

            * Check the checkboxes *Support paired braces*, *Support paired brackets* and *Support parens*

            * Add some *keywords*: `rule`, `when`, `then`, `end`, ...

            * Click button *ok*

        * Next to the list *Registered Patterns*, use the button *Add...*:

            * For `DRL files`, add `*.drl`, `*.mvel`, `*.drt`, `*.dslr`

            * For `Text files`, add `*.md`

            * For `Properties files`, add `*.dsl`

            * For `XML Files`, add `*.rf`

* Optionally import one of our code styles:

    * If you don't do this, you need to set the file encoding and number of spaces correctly manually.

    * Choose the correct one from `droolsjbpm-build-bootstrap/ide-configuration`

        * The `droolsjbpm-knowledge` and `drools` repositories use original, not java-conventions.

    * Copy to `~/.IntelliJIdea10/config/codestyles/` (on mac: `~/Library/Preferences/IntelliJIdea10/config/codestyles/`)

    * Restart, open menu *File*, menu item *Settings*

    * Click tree item *Code Style* and select it.

* Set the correct file encoding (UTF-8 except for properties files) and end-of-line characters (unix):

    * Open menu *File*, menu item *Settings*

    * Click tree item *Code Style*, tree item *General*

        * Combobox *Line separator (for new files)*: `Unix`

    * Click tree item *File Encodings*

        * Combobox *IDE Encoding*: `UTF-8`

        * Combobox *Default encoding for properties files*: `ISO-8859-1`

            * Note: normal i18n properties files must be in `ISO-8859-1` as specified by the java `ResourceBundle` contract.

                * Note on note: GWT i18n properties files override that and must be in `UTF-8` as specified by the GWT contract.

* Set the correct number of spaces when pressing tab:

    * Open menu *File*, menu item *Settings*

    * Click tree item *Code Style*, tree item *General*

    * Click tab *Java*

        * Checkbox *Use tab character*: `off`

        * Textfield *Tab size*: `4`

        * Textfield *Indent*: `4`

        * Textfield *Continuation indent*: `8`

    * Open tab *XML*

        * Checkbox *Use tab character*: `off`

        * Textfield *Tab size*: `2`

        * Textfield *Indent*: `2`

        * Textfield *Continuation indent*: `4`

* Set the correct file headers (do not include @author or a meaningless javadoc):

    * Open menu *File*, menu item *Settings*

    * Click tree item *File templates*, tab *Includes*, list item `File Header`

    * Remove the line *@author Your Name*.

        * We do not accept `@author` lines in source files, see FAQ below.

    * Remove the entire javadoc as automatically templated data is meaningless.

* Set the correct license header

    * Open menu *File*, menu item *Settings*

    * Click tree item *Copyright*, tree item *Copyright profiles*

        * Click button *+* to add a *Copyright profile*

        * Textfield *name*: `JBoss Inc`

        * Textarea with content:

                Copyright $today.year JBoss Inc

                Licensed under the Apache License, Version 2.0 (the "License");
                you may not use this file except in compliance with the License.
                You may obtain a copy of the License at

                      http://www.apache.org/licenses/LICENSE-2.0

                Unless required by applicable law or agreed to in writing, software
                distributed under the License is distributed on an "AS IS" BASIS,
                WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                See the License for the specific language governing permissions and
                limitations under the License.

        * Note: Do not start or end with a newline character

        * Note: Do not start with `/**`: it is not a valid javadoc.

    * Click tree item *Copyright*

        * Combobox *Default project copyright*: `JBoss Inc`

Extra IntelliJ plugins
----------------------

* Enable git support

    * Open menu *File*, menu item *Other Settings*, menu item *Configure plugins*.

    * Check *Git*.

* GWT plugin (to run in GWT hosted mode)

    * Open menu *File*, menu item *Project structure*

        * For the module `guvnor-webapp-drools`, add the new aspect *GWT* if you haven't already.

            * Textfield *Compiler maximum heap size (Mb)*: `512`

    * Open menu *Run*, menu item *Edit configurations*

        * Add new *GWT configuration*

            * Combobox *Module*: `guvnor-webapp-drools`

            * Combobox *GWT Module to load*: `org.drools.guvnor.FastCompiledGuvnor`

            * Textfield *VM options*: `-Xmx1024m -XX:MaxPermSize=256m`

            * Textfield *Start page*: `org.drools.guvnor.Guvnor/Guvnor.html` (Second entry, not the first)

        * Run that configuration.

* Tomcat exploded war deployment

    * Open menu *File*, menu item *Project structure*

        * Select tree item *Artifacts*, list item `guvnor-webapp-drools:war exploded`

            * Checkbox *Build on make*: `on`

    * Open menu *Run*, menu item *Edit configurations*

        * Add new *Tomcat server*, *local*

            * Tab *deployment*, add *Artifact* `guvnor-webapp-drools:war exploded`.

            * Panel *Before launch*, checkbox *Build 'guvnor-webapp-drools:war exploded' artifact*: `on`

        * Run that configuration.

Team communication
==================

To develop a great project as a team, we need to communicate efficiently as a team.

Team workflows
--------------

* Fixing a community issue in JIRA:

    * Find/create the issue in JIRA ([Drools](https://issues.jboss.org/browse/DROOLS),
    [OptaPlanner](https://issues.jboss.org/browse/PLANNER), [jBPM](https://issues.jboss.org/browse/JBPM],
    [Guvnor](https://issues.jboss.org/browse/GUVNOR)

    * Fix the issue and push those changes to the appropriate branch(es) on github.

    * Change the *Status* to `Resolved`.

        * Once the reporter verifies the fix, he changes *Status* to `Closed`. Or we bulk change it to `Closed` after a year.

* (Red Hat developers only) Fixing BRMS issues in Bugzilla:

    * Find an issue in Bugzilla. Change *Status* to `ASSIGNED` and *Assigned To* to yourself.

    * Fix the issue and push those changes to the appropriate branch(es) on github.

        * This will likely require back porting or forward porting, because the issue must be fixed on master too.

    * Change the *Status* to `MODIFIED`.

        * Once the new product version is build, they change *Status* to `ON_QA`.

        * Once QA verifies the fix, they change *Status* to `Verified`.

Knowing what's going on
-----------------------

* **[Subscribe to the dev list](http://www.jboss.org/drools/lists) and check it daily.**

    * Send a summary of every important organizational or structural decision to the dev list.

    * If you (accidentally) push a change that can severely hinder or disrupt other developers (such as a compilation failure), notify the dev list.

* Subscribe to the RSS feeds.

    * **It's recommend to subscribe at least to the RSS feeds of the project/repositories you're working on.**

    * Prefer an RSS reader which shows which RSS articles you've already read, such as:
        
        * Thunderbird

            * Open menu *File*, menu item *Subscribe*.
            
            * Tip: create a new, separate directory for each feed: some feeds (such as about the project you are working on) are more important to you than others.

        * [Google Reader](http://www.google.com/reader)

    * Subscribe to jira issue changes:

        * [DROOLS](https://issues.jboss.org/plugins/servlet/streams?key=DROOLS&os_authType=basic)

        * [PLANNER](https://issues.jboss.org/plugins/servlet/streams?key=PLANNER&os_authType=basic)

        * [JBPM](https://issues.jboss.org/plugins/servlet/streams?key=JBPM&os_authType=basic)

        * [GUVNOR](https://issues.jboss.org/plugins/servlet/streams?key=GUVNOR&os_authType=basic)

    * Subscribe to github repository commits:

        * [droolsjbpm-build-bootstrap](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/commits/master.atom)

        * [droolsjbpm-knowledge](https://github.com/droolsjbpm/droolsjbpm-knowledge/commits/master.atom)

        * [drools](https://github.com/droolsjbpm/drools/commits/master.atom)

        * [optaplanner](https://github.com/droolsjbpm/optaplanner/commits/master.atom)

        * [jbpm](https://github.com/droolsjbpm/jbpm/commits/master.atom)

        * [droolsjbpm-integration](https://github.com/droolsjbpm/droolsjbpm-integration/commits/master.atom)

        * [guvnor](https://github.com/droolsjbpm/guvnor/commits/master.atom)

        * [droolsjbpm-tools](https://github.com/droolsjbpm/droolsjbpm-tools/commits/master.atom)

        * [droolsjbpm-build-bootstrap](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/commits/master.atom)

    * Subscribe to [jenkins](https://hudson.jboss.org/hudson/view/Drools%20jBPM/)

        * with [the Firefox plugin](https://addons.mozilla.org/en-us/firefox/addon/jenkins-build-monitor/) to easily see in your status bar which builds are failing (recommended):

            * After installation, right click on the jenkins icon in the lower right corner.

            * Click menu item *Preferences*, tab *Feed*, textfield *poll interval* `30` *minutes*.

            * Click menu item *Preferences*, tab *Display*, combox *Display* `latest build` *on status bar*.

            * Go to the jenkins job of the projects you're working on:

                * [guvnor](https://hudson.jboss.org/hudson/view/Drools%20jBPM/job/guvnor/)

            * Right click in the lower left corner on the *All* feed link, menu item *Add link to jenkins build monitor*.

        * Otherwise, check [the jenkins website](https://hudson.jboss.org/hudson/view/Drools%20jBPM/) often.

            * Note: the public jenkins is a mirror of the VPN internal Red Hat jenkins and is sometimes stale.

                * If you think this can be the case, check the build times.

* Join us on IRC: irc.codehaus.org #drools #jbpm #guvnor

Writing documentation
=====================

* Optionally install a DocBook editor to write documentation more comfortably, such as:

    * [oXygen](http://www.oxygenxml.com/)

        * Open menu *Options*, menu item *Preferences...*.

        * Click tree item *Global*

            * Combobox *Line separator*: `Unix-like`

        * Click tree item *Format*

            * Checkbox *Detect indent on open*: `off`

            * Checkbox *Indent with tabs*: `off`

            * Combobox *Indent size*: `2`

            * Textfield *Line width - Format and Indent*: `120`

    * [XMLmind](http://www.xmlmind.com/xmleditor/)

        * Open menu *Options*, menu item *Preferences...*.

        * Click tree item *Save*

            * Combobox *Encoding*: `UTF-8`

            * Textfield *Identation*: `2`

            * Textfield *Max. line length*: `120`

            * Checkbox *Before saving, make a backup copy of the file*: `off`

                * To avoid committing backups to source control.

                * Source control history is better than backups.

* To generate the html and pdf output run maven with `-Dfull`:

        $ cd droolsjbpm
        $ cd optaplanner/optaplanner-docs
        $ mvn clean install -Dfull
        ...
        $ firefox target/docbook/publish/en-US/html_single/index.html

* **[Read and follow the documentation guidelines](documentation-guidelines.txt).**

* The Drools Expert manual uses railroad diagrams.
    
    These are generated from a BNF file into images files with the application
    [Ebnf2ps, Automatic Railroad Diagram Drawing](http://www.informatik.uni-freiburg.de/~thiemann/haskell/ebnf2ps/)

Releasing
=========

Expecting a release
-------------------

One week in advance:

* Announce on the upcoming release on all the developer mailing lists and in the IRC channel topics.

    * Include a list of projects on Jenkins that are yellow or red.

        * Daily remind the lead of any project that is red.

    * For a CR/Final, also mention the FindBugs reports on jenkins.

* All external dependencies must be on a non-SNAPSHOT version, to avoid failing to *close* the staging repo on nexus near the end of the release.

    * Get those dependencies (for example `mvel` and `bpm-console`) released if needed, preferably 1 week before the kie release. This way, those released artifacts gets tested by our tests.

* Ask kie-wb module (uberfire, guvor, kie-wb-common, drools-wb, jbpm-console, jbpm-form-modeler and kie-wb-distributions) leads to update the translations with Zanata:

    * Translations into different locales are handled within Zanata (https://translate.jboss.org/)

    * Email Zanata mailing list that a release is about to be made.

    * The most recent translations need to be pulled into the release branch. Assuming you have set-up your Zanata configuration correctly, this can be achieved with:

            $ mvn zanata:pull-module

    * NOTE: If releasing a new version number (major, minor or micro) a new version of the translations should be setup in Zanata.

    * Automatically fix simple errors in the translations using the following:

            $ mvn replacer:replace -N

    * Test compile guvnor to check there are no other translation issues.

            $ mvn clean install -Dfull -DskipTests

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

        $ git-all.sh pull --rebase

* Do a sanity check.

    * Produce the distribution zips, build with `-Dfull`:

            $ droolsjbpm-build-bootstrap/script/mvn-all.sh clean install -Dfull -DskipTests

        * Warning: It is not uncommon to run out of either PermGen space or Heap Space. The following settings are known (@Sept-2012) to work:-

                $ export MAVEN_OPTS='-Xms512m -Xmx2200m -XX:MaxPermSize=512m'

        * Warning: Verify that workspace contains no uncommitted changes or rogue module directories of older branches:

                $ droolsjbpm-build-bootstrap/script/git-all.sh status

            * Specifically watch out for an uncommitted `*/target` directory: that's the result of a build of an older branch that didn't get cleaned.

                * If the root of that directory gets zipped, binaries of that older branch leak into today's distribution zip.

    * Do a sanity check of the artifacts by running each runExamples.sh from the zips.

        * Go to `droolsjbpm-build-distribution/droolsjbpm-uber-distribution/target/*/download_jboss_org`:

            * Unzip the zips to a temporary directory.

            * Start the `runExamples.sh` script for drools, droolsjbpm-integration and optaplanner

            * Deploy the guvnor jboss-as-7.0 war to guvnor and surf to it:

                * Install the mortgages examples, build it and run the test scenario's

            * Verify that the reference manuals open in a browser (HTML) and Adobe Reader (PDF).

Creating a release branch
-------------------------

A release branch name should always end with `.x` so it looks different from a tag name and a topic branch name.

* When do we create a release branch?

    * We only create a release branch just before releasing CR1.

        * For example, just before releasing 6.1.0.CR1, we created the release branch 6.1.x

            * The release branch 6.1.x contained the releases 6.1.0.CR1, 6.1.0.Final, 6.1.1.Final, 6.1.2.Final, ...

    * Alpha/Beta releases are released directly from master, because we don't backport commits to Alpha/Beta's.

* Alert the IRC dev channels that you're going to branch master.

* Pull the latest changes.

        $ git-all.sh pull --rebase

* Simply use the script `script/release/create-release-branches.sh` with the drools and jbpm *release branch name*:

        $ droolsjbpm-build-bootstrap/script/release/create-release-branches.sh 6.1.x 6.1.x 
        where 6.1.x is the drools and 6.1.x is the jbpm release branch name
        
        * Note: this srcript creates a release branch, pushes it to origin and sets the upstream from local release branch to remote release branch
                   

* Switch back and forth from master to the release branches for all git repositories

    * If you haven't made the branches yourself, first make sure your local repository knows about them:

            $ droolsjbpm-build-bootstrap/script/git-all.sh fetch

    * Switch to master with `script/git-checkout-all.sh`

            $ droolsjbpm-build-bootstrap/script/git-checkout-all.sh master master

    * Update master to the next SNAPSHOT version to avoid clashing the artifacts on nexus of master and the release branch:

            $ droolsjbpm-build-bootstrap/script/release/update-version-all.sh 6.1.0-SNAPSHOT 6.2.0-SNAPSHOT 6.1.0-SNAPSHOT 6.2.0-SNAPSHOT

        * Note: the arguments are `droolsOldVersion droolsNewVersion jbpmOldVersion jbpmNewVersion`.

        * WARNING: FIXME the update-version-all script does not work correctly if you are releasing a hotfix version.

        * WARNING: jbpm/pom.xml sometimes has properties defined that override the ${version.org.jbpm}. Check this is not the case.

                $ grep -r '6.1.0-SNAPSHOT' **/pom.xml or for i in $(find . -name "pom.xml"); do grep '6.1.0-SNAPSHOT' $i; done 

        * WARNING: script update-version-all.sh did not update all versions in all modules for 6.2.0.Final. Check all have been updated with the following and re-run if required.

                $ grep -r '6.1.0-SNAPSHOT' **/pom.xml or for i in $(find . -name "pom.xml"); do grep '6.1.0-SNAPSHOT' $i; done 
        
        * Note: in either case it is important to search for -SNAPSHOT, as there are various hidden -SNAPSHOT dependencies in some pom.xml files and they should be prevented for releases        

        * Commit those changes (so you can tag them properly):
        
            * Add changes from untracked files if there are any. WARNING: DO NOT USE "git add ." . You may accidentally add files that are not meant to be added into git. 

                    $ git add {filename}
                
            * Commit all changes
                
                    $ droolsjbpm-build-bootstrap/script/git-all.sh commit -m "Set release version: 6.2.0-SNAPSHOT"
                    
            * Check if all repositories build after version upgrade
    
                    $ sh droolsjbpm-build-bootstrap/mvnall.sh mvn clean install -Dfull -DskipTests
        
    * Push the new -SNAPSHOT version to master of the blessed directory
 
             $ sh droolsjbpbm-build-bootstrap/script/git-all.sh pull --rebase (pulls all changes fro master that could be commited in the meantime and prevents merge problems when pushing commits)
             $ sh droolsjbpm-build-bootstrap/script/git-all.sh push origin master (pushes all commits to master)


    * Switch back to the *release branch name* with `script/git-checkout-all.sh` with drools and jbpm *release branch name*:

            $ sh droolsjbpm-build-bootstrap/script/git-checkout-all.sh 6.1.x 6.1.x

* Push the created release branches to the blessed directory
            
            $ sh droolsjbm-build-bootstrap/script/git-all.sh push origin 6.1.x

* Set up jenkins build jobs for the branch.

    * Go to the internal jenkins website inside the VPN.

    * Clone each of the master build jobs for every git repo that was branched.

        * Suffix the build job name with the branch name, for example `drools-6.1.x` and `droolsjbpm-integration-6.1.x`.

        * Change the build job configuration to use the git repo branch, for example `6.1.x`.

* Set up a new Jenkins view for the related release builds

    * https://jenkins.mw.lab.eng.bos.redhat.com/hudson/me/my-views/view/All/

        * Note: Add all Drools, jBPM and Guvnor jobs manually or use a regex pattern similar to `^((drools|guvnor).*5\.5|jbpm.*5\.4).*$`

* Alert the dev mailing list and the IRC channel that the branch has been made.

    * Remind everyone clearly that every new commit to `master` will not make the upcoming CR and Final release, unless they cherry-pick it to this new branch.


#### NOTE: 
* at this point we have created a release branch
* we have updated the master branch to the new development version (* -SNAPSHOT)
* we have pushed the created release branches to origin
* we have set up a new Jenkins view for the created "release branch" 


Releasing from a release branch
-------------------------------

* Alert the IRC dev channels that you're starting the release.

* Pull the latest changes.

        $ git-all.sh pull --rebase

* Optional: do another sanity check.

If everything is perfect (compiles, jenkins is all blue and sanity checks succeed):

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

            $ droolsjbpm-build-bootstrap/script/release/update-version-all.sh 5.2.0-SNAPSHOT 5.2.0.Final 5.1.0-SNAPSHOT 5.1.0.Final

        * Note: the arguments are `droolsOldVersion droolsNewVersion jbpmOldVersion jbpmNewVersion`.

        * WARNING: FIXME the update-version-all script does not work correctly if you are releasing a hotfix version.

        * WARNING: Guvnor has a hard-coded version number in org.drools.guvnor.server.test.GuvnorIntegrationTest.createDeployment. This must be changed manually and committed.

        * WARNING: script update-version-all.sh did not update all versions in all modules for 5.5.0.Final. Check all have been updated with the following and re-run if required.

                $ grep -r '5.4.0-SNAPSHOT' **/pom.xml or for i in $(find . -name "pom.xml"); do grep '5.4.0-SNAPSHOT' $i; done 

        * Commit those changes (so you can tag them properly):
        
            * Add changes from untracked files if there are any. WARNING: DO NOT USE "git add ." . You may accidentally add files that are not meant to be added into git. 

                    $ git add {filename}
                
            * Commit all changes

                    $ droolsjbpm-build-bootstrap/script/git-all.sh commit -m"Set release version: 5.2.0.Final"

    * Update the *Compatibility matrix* in `droolsjbpm-knowledge/kie-docs/shared/.../Chapter-Compatibility_matrix.xml`.

        * Cherry pick that change to master too.

* Create the tag locally. The arguments are the Drools version, the jBPM version:

        $ droolsjbpm-build-bootstrap/script/release/git-tag-locally-all.sh 5.2.0.Final 5.1.0.Final

* Go to [nexus](https://repository.jboss.org/nexus), menu item *Staging repositories*, drop all your old staging repositories.

* Deploy the artifacts:

        $ droolsjbpm-build-bootstrap/script/mvn-all.sh -Dfull -DskipTests clean deploy

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

    * Define the next development version on the branch from which you are releasing.

        * There are only 1 acceptable pattern:

            * `major.minor.micro-SNAPSHOT`, for example `1.2.0-SNAPSHOT` or `1.2.1-SNAPSHOT`

        * Warning: The release branch should never have the same SNAPSHOT version as any other branch.

            * If you're releasing a Final, increment the micro number, not the minor number.

    * Adjust the version in the poms, manifests and other eclipse stuff:

            $ droolsjbpm-build-bootstrap/script/release/update-version-all.sh 5.2.0.Final 5.3.0-SNAPSHOT 5.1.0.Final 5.2.0-SNAPSHOT

        * Commit those changes:

                $ droolsjbpm-build-bootstrap/script/git-all.sh add .

                $ droolsjbpm-build-bootstrap/script/git-all.sh commit -m"Set next development version: 5.3.0-SNAPSHOT"

        * Push all changes, both the first and the last version change commit, to the repository *together*:

                $ droolsjbpm-build-bootstrap/script/git-all.sh push

        * Warning: Guvnor has a hard-coded version number in org.drools.guvnor.server.test.GuvnorIntegrationTest.createDeployment. This must be changed manually and committed.

        * Warning: script update-version-all.sh did not update all versions in all modules for 5.5.0.Final. Check all have been updated with the following and re-run if required.

                $ grep -r '5.4.0-SNAPSHOT' **/pom.xml or for i in $(find . -name "pom.xml"); do grep '5.4.0-SNAPSHOT' $i; done 

        * Warning: If releasing from master (i.e. a Beta release) and the push fails as there have been other commits to the remote master branch it might be necessary to pull.

                $ droolsjbpm-build-bootstrap/script/git-all.sh pull

* Push the local tag to the remote blessed repository.

        $ droolsjbpm-build-bootstrap/script/release/git-push-tag-all.sh 5.2.0.Final 5.1.0.Final

    * Push your changes to the release branch:

        * Especially if the release branch is master: First pull any latest changes **without `--rebase`**, .

                $ git-all.sh pull

            * Without the `--rebase` it's a merge, and their commits will not be rebased before your version-changing commits.

        * Push your version-changing commits to the release branch:

                $ git-all.sh push origin 5.2.x

* Release your staging repository on [nexus](https://repository.jboss.org/nexus)

    * Button *release*

* Go to [jira](https://issues.jboss.org) and for each of our JIRA projects (DROOLS, PLANNER, JBPM, GUVNOR):

    * Open menu item *Administration*, link *Manage versions*, release the version.

    * Create a new version if needed. There should be at least 2 unreleased non-FUTURE versions.

* Upload the zips, documentation and javadocs to filemgmt and update the website.

    * Go to `droolsjbpm-build-distribution/droolsjbpm-uber-distribution/target`.

    * To get access to `filemgmt.jboss.org`, see preparation above.

    * Folder `download_jboss_org` should be uploaded to `filemgmt.jboss.org/downloads_htdocs/drools/release`
    which ends up at [download.jboss.org](http://download.jboss.org/drools/release/)

        * Update [the download webpage](http://www.jboss.org/drools/downloads) accordingly.

    * Folder `docs_jboss_org` should be uploaded to `filemgmt.jboss.org/docs_htdocs/drools/release`
    which ends up at [docs.jboss.org](http://download.docs.org/drools/release/)

        * Use `documentation_table.txt` to update [the documentation webpage](http://www.jboss.org/drools/documentation).

* Update the symbolic links `latest` and `latestFinal` links on filemgmt, if and only if there is no higher major or minor release was already released.

        $ droolsjbpm-build-bootstrap/script/release/create_filemgmt_links.sh 5.2.0.Final

    * Wait 5 minutes and then check these URL's. Hit ctrl-F5 in your browser to do a hard refresh:

        * [http://download.jboss.org/drools/release/latest/](http://download.jboss.org/drools/release/latest/)

        * [http://download.jboss.org/drools/release/latestFinal/](http://download.jboss.org/drools/release/latestFinal/)

        * [http://docs.jboss.org/drools/release/latest/](http://docs.jboss.org/drools/release/latest/)

        * [http://docs.jboss.org/drools/release/latestFinal/](http://docs.jboss.org/drools/release/latestFinal/)

Announcing the release
----------------------

* Create a blog entry on [the droolsjbpm blog](http://blog.athico.com/)

    * Include a direct link to the new and noteworthy section and to that blog entry in all other correspondence.

    * Twitter and Google+ the links.

        * Most people just want to read the new and noteworthy, so link that first.

    * Mail the links to the user list.

* If it's a Final, non-hotfix release:

    * Notify TheServerSide and Dzone's Daily Dose.
    

Synching the Product Repository
===============================

The community code repositories under the @droolsjbpm account contains all the code released as part of the community projects for Drools and jBPM. Every time a new minor or major version is released, a new community branch is created for that version. For instance, at the time of this writing, we have, for instance, branches *6.0.x*, *5.6.x*, *5.5.x*, etc for each minor/major version released and the *master* branch for future releases.

Red Hat also has a mirror private repository that is used as a base for the product releases. This mirror repository contains all the code from the community repositories, plus a few product specific commits, comprising branding commits (changing names, for instance from Drools to BRMS), different icons/images, etc.

Every time a new product build is required, a request for synching the repositories is made and a new tag has to be created to serve as the base of the new product build.

This new tag will usually be based on the HEAD of a specific community branch with the product specific commits applied on top of it. 

At the time of this writing, the product version 6.0.1.GA is being built. Each tag is then created based on the 6.0.x branch with the prod-6.0.1.GA.x-YYYY.MM.DD rebased on top of it, where YYYY.MM.DD is the year/month/day the branch was created.

Follows a step-by-step instruction on how to do that. These instructions assume:

* You have a local clone of all Drools/jBPM repositories (19 at the time of this writing).
* The clones have a remote repository reference to the @droolsjbpm repositories that we will name **main**
* The clones have a remote repository reference to the @jboss-integration mirrors of these repositories that we will name **prod**

Here are the steps:

**1 - cd into the scripts directory**
```
$ cd droolsjbpm-build-bootstrap/script
```
**2 - Fetch the changes from the _main_ repository:**
```
$ ./git-all.sh fetch main
```
**3 - Rebase the corresponding branches (master and 6.0.x at the time of this writing, and 0.3.x branch for Uberfire)**
```
$ ./git-all.sh rebase main/master master
$ ./git-all.sh rebase main/6.0.x 6.0.x
```
The second command above will raise an error in the Uberfire repository as the branch in Uberfire is named 0.3.x. Ignore the error and in another shell, cd into the uberfire folder and manually rebase Uberfire:
```
$ cd <uberfire clone directory>
$ git rebase main/0.3.x 0.3.x
```
**4 - Fetch the changes from the _prod_ repository:**
```
$ ./git-all.sh fetch prod
```
At the time of this writing, there are only 4 repositories that contain product specific branches. The fetch should only return changes, if it returns, in those 4 repositories. In case any change is picked up in any other repository or in any branch that is not the product branch, someone made a mistake and commited changes to the product repository. This has to be fixed. The 4 repositories are:
```
jbpm-console-ng
dashboard-builder
jbpm-dashboard
kie-wb-distribution
```
**5 - For each of the 4 repositories, in another shell, rebase the product branch:**
```
$ cd <repository>
$ git rebase prod/prod-6.0.1.GA.x-2014.02.10 prod-6.0.1.GA.x-2014.02.10
```
Please note that the above has to be done for each repository that contains product specific branches. Please also note that the product branch name might be different. The example above uses the branch name at the time of this writing.

**6 - Checkout the branch that will serve as the base for the tag on all repositories. This might be a release branch in case the tag will be created based on a community release, or it can be a regular branch like 6.0.x (0.3.x in case of Uberfire):**
```
$ ./git-all.sh checkout 6.0.x
```
The above will raise an error for Uberfire, so in another shell do:
```
$ cd <uberfire folder>
$ git checkout 0.3.x
```
**7 - Create a branch to base the tag on. I usually name the base branch as "bsync.YYYY.MM.DD" where YYYY.MM.DD is the year, month and day when the tag is being created.**
```
$ ./git-all.sh checkout -b bsync.2014.10.12
```
**8 - For each repository with a product specific branch, it is necessary to rebase the product branch on top of the base code. There are several different ways to do that. I prefer to reset the tag branch to the product branch and then rebase it. Here are the steps to do that. In another shell, cd into the repository that contains the product branch, reset the current release branch to the product branch, rebase it on top of the base branch.**
```
$ cd <repository folder>
$ git reset --hard prod-6.0.1.GA.x-2014.02.10
$ git rebase 6.0.x
```
Please note that the example above uses the same branch names used in setp (5) for product branch and (6) for the base branch.
If the rebase creates any conflicts, fix the conflicts and continue the rebase.

**9 - If any conflict happened in step 8, then we need to create new product branches. For each repository with a product branch, cd into the repository folder, create a new product branch and checkout the tag branch again.**
```
$ cd <repository folder>
$ git checkout -b prod-6.0.1.GA.x-2014.02.12
$ git checkout bsync.2014.10.12
```
**10 - If there are any commits that have to be manually cherry-picked into the tag, cd into the corresponding repository and cherry-pick the commit. This should not happen often, but sometimes it does.**
```
$ cd <repository>
$ git cherry-pick -x <SHA>
```
**11 - Build the code for all repositories and test to make sure it is working. Fix any problems in case it is not working. **

**12 - Create the tag for all repositories. For product tags, we use a naming standard of "sync.YYYY.MM.DD", where YYYY.MM.DD is the date the tag is created. If for any reason more than one tag needs to be created on the same day, add a sequential counter sufix: "sync.YYYY.MM.DD.C"**
```
$ ./git-all.sh tag sync.2014.02.12
```
**13 - Push the tag and branches to the _prod_ server.**
```
$ ./git-all.sh push prod sync.2014.02.12
$ ./git-all.sh push prod 6.0.x
$ ./git-all.sh push prod master
```
**14. In case a new product branch was created in step 9, push the new product branch and delete the old remote branch:**
```
$ git push prod-6.0.1.GA.x-2014.02.12
$ git push :prod-6.0.1.GA.x-2014.02.10
```
Please note that this will not delete the old local product branch. I usually leave the local branch around for a few weeks just in case some mistake happened, as it will make it easier to fix, but it can be deleted.


FAQ
===

* Why do you not accept `@author` lines in your source code?

    * Because the author tags in the java files are a maintenance nightmare

        * A large percentage is wrong, incomplete or inaccurate.

        * Most of the time, it only contains the original author. Many files are completely refactored/expanded by other authors.

        * Git is accurate, that is the canonical source to find the correct author.

    * Because the author tags promote *code ownership*, which is bad in the long run.

        * If people work on a piece they perceive as being owned by someone else, they tend to:

            * only fix what they are assigned to fix, instead of everything that's broken

            * discard responsibility if that code doesn't work properly

            * be scared of stepping on the feet of the owner.

        * For more motivation, see [this video on How to get a healthy open source project?](http://video.google.com/videoplay?docid=-4216011961522818645#)

    * Credit to the authors is given:

        * on [the team page](http://www.jboss.org/drools/team)

             * Please contact Geoffrey (or any of us) if you want to add/change/expand your entry in the team page. Don't be shy!

        * on [the blog](http://blog.athico.com)

            * Write an article about the improvements you did! Contact us if you don't have write authorization on the blog yet.

        * with [ohloh](https://www.ohloh.net/p/jboss-drools/contributors) which also has statistics

        * in [the github web interface](https://github.com/droolsjbpm).
