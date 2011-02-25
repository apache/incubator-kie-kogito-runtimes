Developing Drools and jBPM
==========================

**If you want to build or contribute to a droolsjbpm projects, [read this document](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/README.md).**

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

* **Building with Maven**

* **Developing with Eclipse**

* **Developing with IntelliJ**

* **Source control with Git**

* **Releasing**

* **FAQ**

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
                $ ln -s apache-maven-3.0.1 apache-maven

            Next time you only have to remove the link and add it to the new version.

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

    Verify the mavens and java versions.

Running the build
-----------------

* Go into the project's base directory, for example `guvnor`:

        $ cd guvnor
        $ ls

    Notice you see a `pom.xml` there. Those `pom.xml` files are the heart of Maven.

* Run the build:

        $ mvn -DskipTests clean install

    The first build will take a long time, because a lot of dependencies will be downloaded (and cached locally).

    It might even fail, if certain servers are offline or experience hiccups.
    In that case, you 'll see an IO error, so just run the build again.

    **After the first successful build, any next build should be fast and stable.**

* Try running a different profile by using the option `-D<profileActivationProperty>`:

        $ mvn -Dfull -DskipTests clean install

    There are 3 profile activation properties:

    * *none*: Fast, for during development

    * `full`: Slow, but builds everything (including documentation). Used by hudson and during releases.

    * `soa`: prunes away the non-enterprise stuff

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

* Follow [the installation instructions of m2eclipse](http://m2eclipse.sonatype.org/)

    * Follow the link *Installing m2eclipse* at the bottom.

* Click menu *File*, menu item *Import*, tree item *Maven*, tree item *Existing Maven Projects*.

* Open the top level project `pom.xml` file with the m2eclipse plugin.

* Select the profiles `notSoaProfile` and `fullProfile`.

Configuring the project with the deprecated maven-eclipse-plugin
----------------------------------------------------------------

The maven-eclipse-plugin plugin is a plugin in Maven for Eclipse.
This is the old way (of which the development has stopped).

Run this command to generate `.project` and `.classpath` files:

    $ mvn eclipse:eclipse

* Open Eclipse

* Menu item *Import existing projects*, navigate to the project base directory, select all the projects (= modules) it lists.

Important note: `mvn eclipse:eclipse` does not work for our eclipse plugin because it is not compatible with tycho
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

    * Note: i18n properties files must be in `ISO-8859-1` as specified by the java `ResourceBundle` contract.

* Set the correct number of spaces when pressing tab:

    * Warning: If you imported the `eclipse-formatter.xml` file, you don't need to set it for Java, but you do need to set it for XML anyway!

    * Open menu *Window*, menu item *Preferences*.

        * If you have project specific settings enabled instead, right click on the project and click the menu item *Properties*.

    * Click tree item *Java*, tree item *Code Style*, tree item *Formatter*.

        * Click button *Edit* of the active profile, tab *Indentation*

        * Combobox *Tab policy*: `spaces only`

        * Indentation size: `4`

        * Tab size: `4`

        * Note: If it is a build-in profile, you 'll need to change its name with the textfield on top.

    * Click tree item *XML*, tree item *XML Files*, tree item *Editor*.

        * Radio button *Indent using space*: `on`

        * Indentation size: `2`

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

    * Open the main `pom.xml` file from the project base directory.

    * Select the profiles `notSoaProfile` and `fullProfile`.

    * Go grab a coffee while it's indexing.

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

Source control with git
=======================

* First fork the repository you want to work on, for example `guvnor`:

    * Surf to [the blessed repositories on github](https://github.com/droolsjbpm) and log in.

    * Surf to [the specific repository (guvnor)](https://github.com/droolsjbpm/guvnor)

    * Click the top right button *Fork*

* Then clone that fork locally:

        # First make a directory
        $ mkdir droolsjbpm
        $ cd droolsjbpm
        $ git clone git@github.com:MY_GITHUB_USERNAME/guvnor.git guvnor
        $ cd guvnor
        $ ls

* Another Linux Tip:

    You can create a symblink to git-all.sh script to directory that is the PATH (drools-git is a good name), for example ~/bin.
    Then if your distro has git completions scripts for the console you can add the following line to your ~/.bashrc:   
        `$ complete -o bashdefault -o default -o nospace -F _git drools-git`


Releasing
=========

Use JDK 1.6, because in JDK 1.5 the module `guvnor-repository-connector-modeshape` is not build.

To produce distribution builds use:

    $ mvn -Dfull clean install
    $ mvn -Dfull -DskipTests package javadoc:javadoc assembly:assembly

Note: That install must be done first as `javadoc:javadoc` won't work unless the
jars are in the local maven repo, but the tests can be skipped on the second run.

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
