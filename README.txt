Welcome to Drools
=================

Read this document if you want to build or contribute to the drools project.

Building with maven
===================

Drools uses maven to build the system. There are two profiles available which
enable the associated modules "documentation" and "eclipse"; this enables
quicker building of the core modules for developers. The eclipse profile will
download eclipse into the drools-eclipse folder, which is over 100MB download,
however this only needs to be done once; if you wish you can move that eclipse
download into another location and specify it with
-DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror.

The following builds all the jars, the documentation and the eclipse zip with a
local folder specified to avoid downloading eclipse:
 mvn -Declipse -Ddocumentation clean install 
     -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror

You can produce distribution builds, which puts everything into zips, as follows:
mvn -Declipse -Ddocumentation clean install
    -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror
mvn -Ddocumentation -Declipse -DskipTests package javadoc:javadoc assembly:assembly 
    -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror

Note that install must be done first as javadoc:javadoc won't work unless the
jars are in the local maven repo, but the tests can be skipped on the second run.

assembly:assembly fails unless you increase the available memory to Maven, on windows 
the following command worked well:
set MAVEN_OPTS=-Xmx512m

Configuring Eclipse
===================

Code style
----------

Correct number of spaces for tabs:
- Open menu "Window", menu item "Preferences".
- Open tree item "Java", tree item "Code Style", tree item "Formatter".
-- If you imported the trunk/eclipse-formatter.xml file,
   you don't need to set it here,
   but you do need to set it for XML anyway!
-- Click button "Edit" of the active profile
-- Tab "Indentation"
--- Combobox "Tab policy": spaces only
--- Indentation size: 4
--- Tab size: 4
-- If it is a build in profile, you need to change its name with the textfield on top
- Open tree item "XML", tree item "XML Files", tree item "Editor".
-- Radio button "Indent using space": on
-- Indentation size: 2

Correct file encoding (UTF-8 except for properties files) and EOL (unix):
- Open menu "Window", menu item "Preferences".
- Open tree item "General", tree item "Workspace".
-- Label "Text file encoding", radiobutton "Other", combobox "UTF-8"
-- Label "New text file delimiter", radiobutton "Other", combobox "Unix"
- Open tree item "XML", tree item "XML Files".
-- Combobox "Encoding": ISO 10646/Unicode(UTF-8)
- Open tree item "CSS", tree item "CSS Files".
-- Combobox "Encoding": ISO 10646/Unicode(UTF-8)
- Open tree item "HTML", tree item "HTML Files".
-- Combobox "Encoding": ISO 10646/Unicode(UTF-8)
- Note: i18n properties files must be in ISO-8859-1 as specified by the java ResourceBundle contract.

License header
--------------

Eclipse JEE Helios currently has no build-in support of license headers,
but you can configure it for new files.
- Open menu "Window", menu item "Preferences".
- Open tree item "Java", tree item "Code Style", tree item "Copy templates".
-- Open tree item "Comments", tree item "Files".
-- Replace the text area with this:
/*
 * Copyright 2010 JBoss Inc
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
-- Do not start or end with a newline character
-- Update the year (2010) every year.

Configuring IntelliJ
====================

Code style
----------

Correct number of spaces for tabs:
- Open menu "File", menu item "Settings".
- Open tree item "Code Style", tree item "General".
- Open tab "Java"
-- Checkbox "Use tab character": off
-- Textfield "Tab size": 4
-- Textfield "Indent": 4
-- Textfield "Continuation indent": 8
- Open tab "XML"
-- Checkbox "Use tab character": off
-- Textfield "Tab size": 2
-- Textfield "Indent": 2
-- Textfield "Continuation indent": 4

Correct file encoding (UTF-8 except for properties files) and EOL (unix):
- Open menu "File", menu item "Settings".
- Open tree item "Code Style", tree item "General".
-- Combobox "Line seperator (for new files)": Unix
- Open tree item "File Encodings".
-- Combobox "IDE Encoding": "UTF-8"
-- Combobox "Default encoding for properties files": ISO-8859-1

License header
--------------

- Open menu "File", menu item "Settings".
- Open tree item "Copyright", tree item "Copyright profiles".
- Add Copyright profile
-- Textfield name: JBoss Inc
-- Fill this into the text area:
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
-- Do not start or end with a newline character
- Open tree item "Copyright"
-- Combobox "Default project copyright": JBoss Inc

Other notes
===========

If you have a ydoc license then you can build the javadocs with uml images using the ydoc doclet. 
Simple add the following to the mvn command line:
-Dydoc.home=<path to ydoc>

Known problems
==============

* Functions can't be called from MVEL code blocks. Although, static methods
from previously existing classes are working fine.

* There are still some issues with MVEL code completion.

