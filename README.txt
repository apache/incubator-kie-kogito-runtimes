Welcome to Drools
-----------------

Drools uses maven to build the system. There are two profiles available which
enable the associated modules "documentation" and "eclipse"; this enables
quicker building of the core modules for developers. The eclipse profile will
download eclipse into the drools-eclipse folder, which is over 100MB download,
however this only needs to be done once; if you wish you can move that eclipse
download into another location and specify it with
-DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror.

The following builds all the jars, the documentation and the eclipse zip with a
local folder specified to avoid downloading eclipse:
 mvn -Declipse=true -Ddocumentation=true clean install 
     -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror

You can produce distribution builds, which puts everything into zips, as
follows:
mvn -Declipse=true -Ddocumentation=true clean install
    -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror
mvn -Ddocumentation -Declipse -Dmaven.test.skip package javadoc:javadoc
     assembly:assembly -DlocalEclipseDrop=/folder/jboss-rules/local-eclipse-drop-mirror

Note that install must be done first as javadoc:javadoc won't work unless the
jars are in the local maven repo, but the tests can be skipped on the second run.