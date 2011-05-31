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

* **Source control with Git**

* **Building with Maven**

* **Developing with Eclipse**

* **Developing with IntelliJ**

* **Team communication**

* **Writing documentation**

* **Releasing**

* **FAQ**

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

Because you 'll probably want to change our code, it's recommened to fork our code before cloning it,
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

                $ git checkout 5.2.0

* Add the blessed repository as upstream (if you've directly cloned the blessed repository, don't do this):

        $ git remote add upstream git@github.com:droolsjbpm/guvnor.git
        $ git fetch upstream

Working with git
----------------

* Commit and push your changes to your fork

        $ git commit -m"Fix typo in documentation"
        $ git push

* Get the latest changes from the blessed repository

        $ git fetch upstream
        $ git merge upstream/master

    * If there are merge conflicts:

            $ git mergetool
            $ git commit
            
        or

            $ gedit conflicted-file.txt
            $ git add conflicted-file.txt
            $ git commit

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

A pull request is like a patch file, but easier to apply, more powerfull and you'll be credited as the author.

* Creating a pull request

    * Push all your commits to a topic branch on your fork on github (if you haven't already).

        * You can only have 1 pull request per branch, so it's advicable to use topic branches to avoid mixing your changes.

    * Surf to that topic branch on your fork on github.

    * Click the button *Pull Request* on the top of the page.

* Accepting a pull request

    * Surf to the pull request page on github.

    * Review the changes

    * Click the button *Merge help* on the bottom of the page and follow the instructions of github to apply those changes on master.

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
        drools  droolsjbpm-build-bootstrap  droolsjbpm-build-distribution  droolsjbpm-integration  droolsjbpm-knowledge  droolsjbpm-tools  drools-planner  guvnor
        $ cd guvnor
        $ ls
        ...  guvnor-repository  guvnor-webapp  pom.xml

    Notice you see a `pom.xml` file there. Those `pom.xml` files are the heart of Maven.

* **Run the build**:

        $ mvn clean install -DskipTests

    The first build will take a long time, because a lot of dependencies will be downloaded (and cached locally).

    It might even fail, if certain servers are offline or experience hiccups.
    In that case, you 'll see an IO error, so just run the build again.

    After the first successful build, any next build should be fast and stable.

* Try running a different profile by using the option `-D<profileActivationProperty>`:

        $ mvn clean install -DskipTests -Dfull

    There are 3 profile activation properties:

    * *none*: Fast, for during development

    * `full`: Slow, but builds everything (including documentation). Used by hudson and during releases.

    * `soa`: prunes away the non-enterprise stuff

* To run a maven build over all repositories (only works if you cloned all repositories):

        $ cd ~/projects/droolsjbpm
        $ droolsjbpm-build-bootstrap/script/mvn-all.sh -DskipTests clean install

    * Note: the `mvn-all.sh` script is working directory independent.

* Warning: The first `mvn` build of a day will download the latest SNAPSHOT dependencies of other droolsjbpm projects,
unless you build all those droolsjbpm projects from source.
Those SNAPSHOTS were build and deployed last night by hudson jobs.

    * If you 've pulled all changes (or cloned a repository) today, this is a good thing:
    it saves you from having to download and build all those other latest droolsjbpm projects from source.

    * If you haven't pulled all changes today, this is probably a bad thing:
    you 're probably not ready to deal with those new snapshots.

        In that case, add `-nsu` (= `--no-snapshot-updates`) to the `mvn` command to avoid downloading those snapshots:

            $ mvn clean install -DskipTests -nsu

        Note that using `-nsu` will also make the build faster.

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

More info in [the JBoss.org guide to get started with Maven](http://community.jboss.org/wiki/MavenGettingStarted-Developers).

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

* Only on Mac, also add these lines to avoid a Mac-specific p2 bug with tycho 0.11.0 in droolsjbpm-tools:

        -startup
        ../../../plugins/org.eclipse.equinox.launcher_1.1.0.v20100507.jar
        --launcher.library
        ../../../plugins/org.eclipse.equinox.launcher.cocoa.macosx.x86_64_1.1.1.R36x_v20100810

    TODO: Remove this note when we upgrade to tycho 0.12.0 (if that uses a p2 director that fixes it)

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

        * Open menu *Run*, menu item *Run configurations...*

        * In the list, select *Web Application*, button *new launch configuration*

        * Tab *Main*, Project: `guvnor-webapp`

        * Tab *GWT*, list *Available Modules*: `Guvnor - org.drools.guvnor`

        * Tab *GWT*, textfield *URL*: `org.drools.guvnor.Guvnor/Guvnor.html`

        * Button *Run*.

    * In your workspace, in the tab *Development Mode*, double click on the `Guvnor` URL.

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

* GWT plugin

    * Open menu *File*, menu item *Project structure*

    * For the module `guvnor-webapp`, add the new aspect *GWT* if you haven't already.

    * Open menu *Run*, menu item *Edit configurations*

    * Add new *GWT configuration*, combobox *module* `guvnor-webapp`. Run that configuration.

Team communication
==================

To develop a great project as a team, we need to communicate efficiently as a team.

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

        * [JBRULES](https://issues.jboss.org/plugins/servlet/streams?key=JBRULES&os_authType=basic)

        * [JBPM](https://issues.jboss.org/plugins/servlet/streams?key=JBPM&os_authType=basic)

        * [GUVNOR](https://issues.jboss.org/plugins/servlet/streams?key=GUVNOR&os_authType=basic)

    * Subscribe to github repository commits:

        * [droolsjbpm-build-bootstrap](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/commits/master.atom)

        * [droolsjbpm-knowledge](https://github.com/droolsjbpm/droolsjbpm-knowledge/commits/master.atom)

        * [drools](https://github.com/droolsjbpm/drools/commits/master.atom)

        * [drools-planner](https://github.com/droolsjbpm/drools-planner/commits/master.atom)

        * [jbpm](https://github.com/droolsjbpm/jbpm/commits/master.atom)

        * [droolsjbpm-integration](https://github.com/droolsjbpm/droolsjbpm-integration/commits/master.atom)

        * [guvnor](https://github.com/droolsjbpm/guvnor/commits/master.atom)

        * [droolsjbpm-tools](https://github.com/droolsjbpm/droolsjbpm-tools/commits/master.atom)

        * [droolsjbpm-build-bootstrap](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/commits/master.atom)

    * Subscribe to [hudson](https://hudson.jboss.org/hudson/view/Drools%20jBPM/)

        * with [the Firefox plugin](https://addons.mozilla.org/en-us/firefox/addon/jenkins-build-monitor/) to easily see in your status bar which builds are failing (recommended):

            * After installation, right click on the hudson icon in the lower right corner.

            * Click menu item *Preferences*, tab *Feed*, textfield *poll interval* `30` *minutes*.

            * Click menu item *Preferences*, tab *Display*, combox *Display* `latest build` *on status bar*.

            * Go to the hudson job of the projects you're working on:

                * [guvnor](https://hudson.jboss.org/hudson/view/Drools%20jBPM/job/guvnor/)

            * Right click in the lower left corner on the *All* feed link, menu item *Add link to jenkins build monitor*.

        * Otherwise, check [the hudson website](https://hudson.jboss.org/hudson/view/Drools%20jBPM/) often.

            * Note: the public hudson is a mirror of the VPN internal Red Hat hudson and is sometimes stale.

                * If you think this can be the case, check the build times.

* Join us on IRC: irc.codehaus.org #drools #jbpm #guvnor

Writing documentation
=====================

* Optionally install a DocBook editor to write documentation more comfortably, such as:

    * [XMLmind Personal Edition](http://www.xmlmind.com/xmleditor/download.shtml)

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
        $ cd guvnor/guvnor-docs
        $ mvn clean install -Dfull
        ...
        $ firefox target/docbook/publish/en-US/html_single/index.html

* **[Read and follow the documentation guidelines](documentation-guidelines.txt).**

Releasing
=========

Expecting a release
-------------------

* Use a non-SNAPSHOT version for all dependencies. Get those dependencies (for example mvel) released if needed.

* Run findbugs on all projects:

        $ mvn site
        $ firefox */target/site/findbugs.html

Creating a release branch
-------------------------

A release branch name should always end with `.x` so it looks different from a tag name and a topic branch name.

* Simply use the script `script/branches/create-release-branches.sh` with the drools and jbpm *release branch name*:

        $ droolsjbpm-build-bootstrap/script/branches/create-release-branches.sh 5.2.x 5.1.x

    * It does something like this for every repository:

            $ git checkout -b 5.2.x
            $ git push origin 5.2.x

* Switch back and forth from master to the release branches for all git repositories

    * If you haven't made the branches yourself, first make sure your local repository knows about them:

            $ droolsjbpm-build-bootstrap/script/git-all.sh fetch

    * Use `script/branches/git-checkout-all.sh` with the drools and jbpm *release branch name*:

            $ droolsjbpm-build-bootstrap/script/branches/git-checkout-all.sh master master
            $ droolsjbpm-build-bootstrap/script/branches/git-checkout-all.sh 5.2.x 5.1.x

Releasing from a release branch
-------------------------------

Warning: Use JDK 1.6, because in JDK 1.5 the module `guvnor-repository-connector-modeshape` is not build.

To produce the distribution zips, build with `-Dfull`:

    $ droolsjbpm-build-bootstrap/script/mvn-all.sh -Dfull clean install

The distribution zips are in the directory `droolsjbpm-build-distribution/droolsjbpm-uber-distribution/target`.

If everything is perfect (tested by QA etc):

* Define the version and adjust the poms.

    * Note: Always use at least 3 numbers in the version: '1.0.0' is fine, `1.0` is not fine.

    * Search all the files for `x.y.z-SNAPSHOT`, `x.y.z.SNAPSHOT` and `x.y.z.qualifier` where `x.y.z` is the version.

        * You should find that at least in `pom.xml`, in drooljbpm-tools `MANIFEST.MF` files and in the osgi-bundles.

            * Note: unlike many of the other `MANIFEST.MF` files, those in droolsjbpm-tools are *not* generated.

        * Replace that with `x.y.z` (or `x.y.z.M1` or `x.y.z.CR1`)

            * Excluding generated files (for example the drools `MANIFEST.MF` files, but not those of droolsjbpm-tools)

* Create the tag locally:

        $ droolsjbpm-build-bootstrap/script/branches/git-tag-locally-all.sh 5.2.0 5.1.0

* Deploy the artifacts:

        $ droolsjbpm-build-bootstrap/script/mvn-all.sh -Dfull -DskipTests clean deploy

        * The release skips the tests because jbpm and guvnor have random failing tests

* Go to [nexus](https://repository.jboss.org/nexus), menu item *Staging repositories*, find your staging repository.

    * Look at the files in the repository

        * Right click on `org/drools/org.eclipse.webdav/` and delete it if it's still version `3.0.101`. TODO FIXME

    * Button *close*

        * This will validate the nexus rules. If any fail: fix the issues, and force retag locally.

* Do a sanity check of the artifacts.

    * Go to `droolsjbpm-build-distribution/droolsjbpm-uber-distribution/target` and check the zips

        * Start the `examples.sh` script for drools, droolsjbpm-integration and drools-planner

        * Deploy the guvnor jboss-as-5.1 war to guvnor and surf to it:

            * Install the mortgages examples, build it and run the test scenario's

        * Warning: the manual dirs have been known to have zip problems: they look fine zipped, but are empty unzipped.

* This is **the point of no return**.

    * Warning: The slightest change after this requires the use of the next version number!

        * **NEVER TAG OR DEPLOY A VERSION THAT ALREADY EXISTS AS A PUSHED TAG OR A DEPLOY!!!**

            * Except deploying `SNAPSHOT` versions.

            * Git tags are cached on developer machines forever and are never refreshed.

            * Maven non-snapshot versions are cached on developer machines and proxies forever and are never refreshed.

        * So even if the release is broken, do not reuse the same version number! Create a hotfix version.

* Push the tag to the blessed repository.

        $ droolsjbpm-build-bootstrap/script/branches/git-push-tag-all.sh 5.2.0 5.1.0

* Release your staging repository on [nexus](https://repository.jboss.org/nexus)

    * Button *release*

* Go to [jira](https://issues.jboss.org) and for each of our JIRA projects (JBRULES, GUVNOR, JBPM):

    * Open menu item *Administration*, link *Manage versions*, release the version.

    * Create new versions if needed.

* Prepare the next development iteration

    * Get `x.y.z-SNAPSHOT`, `x.y.z.SNAPSHOT` and `x.y.z.qualifier` back on the correct places

        * Easiest way is to revert the specific commit that changed them with `git revert commitId`.

* Announce the release:

    * Announce it on [the droolsjbpm blog](http://blog.athico.com/)

        * Twitter the blog link.

        * Mail the blog link to the user list.

    * If it's a final release, announce it on Dzone's daily dose and TheServerSide.

Notifying people of the release
-------------------------------

* Create a blog entry

    * Include a link to that blog entry in all other correspondence.

* Send a mail to the BRMS QE Lead (Lukáš Petrovický)

* If it's final release:

    * Notify TheServerSide and Dzone's Daily Dose.

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
