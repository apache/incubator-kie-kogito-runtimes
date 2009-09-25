Welcome to Drools
-----------------

Drools uses maven to build the system. There are two profiles available which
enable the associated modules "documentation" and "eclipse"; this enables
quicker building of the core modules for developers. The eclipse profile will
download eclipse into the drools-eclipse folder, which is over 100MB download,
however this only needs to be done once; if you wish you can move that eclipse
download into another location and specify it with
-DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror.

NOTE: you MUST use maven version 2.0.9 or 2.0.10 to build because of surefire
maven plugin classpath problems.

The following builds all the jars, the documentation and the eclipse zip with a
local folder specified to avoid downloading eclipse:
 mvn -Declipse -Ddocumentation clean install 
     -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror

You can produce distribution builds, which puts everything into zips, as
follows:
mvn -Declipse -Ddocumentation clean install
    -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror
mvn -Ddocumentation -Declipse -DskipTests package javadoc:javadoc assembly:assembly 
    -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror

Note that install must be done first as javadoc:javadoc won't work unless the
jars are in the local maven repo, but the tests can be skipped on the second run.

assembly:assembly fails unless you increase the available memory to Maven, on windows 
the following command worked well:
set MAVEN_OPTS=-Xmx512m

If you have a ydoc license then you can build the javadocs with uml images using the ydoc doclet. 
Simple add the following to the mvn command line:
-Dydoc.home=<path to ydoc>

KNOWN PROBLEMS:

* Functions can't be called from MVEL code blocks. Although, static methods
from previously existing classes are working fine.

* There are still some issues with MVEL code completion.

