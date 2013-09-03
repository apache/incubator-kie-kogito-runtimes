Custom distribution for JBoss EAP based on static modules
==========================================================

BPMS/BRMS application are distributed using skinny WARs for JBoss EAP 6.1, JBoss AS7, etc. But these distributions depend only on JBoss EAP base static modules.

This script allows to create a BPMS/BRMS distribution based on base EAP modules and custom new generated ones. So, this distribution contains:
* A new static module layer for JBoss EAP called <code>bpms</code>
* Skinny WARS for BPMS/BRMS that depend on base and <code>bpms</code> layer

Currenly the supported EAP versions are:
* JBoss EAP 6.1.0.GA

Goals
-----

This script creates a zip file that cotains:
* BPMS JBoss EAP Layer distribution
* Lightweight skinny WARs

BPMS Layer distribution
-----------------------

The BPMS static modules layer distribution adds the follosing modules:

* org.kie.lib
* org.kie
* org.sonatype.aether
* org.apache.ant
* org.apache.camel
* org.codehouse.plexus
* org.apache.commons.math
* org.apache.commons.compress
* org.drools
* org.apache.commons.exec
* org.apache.helix
* org.jbpm
* org.eclipse.jgit
* org.apache.lucene
* org.apache.maven
* org.mvel
* org.apache.commons.net
* org.apache.poi
* com.google.protobuf
* org.sonatype.sisu
* org.jboss.solder
* org.sonatype.maven
* org.sonatype.plexus
* org.apache.commons.vfs (Not in BRMS ditribution)
* org.apache.maven.wagon
* org.apache.zookeeper
* org.apache.batik
* org.apache.commons.httpclient
* org.apache.commons.fileupload
* org.apache.commons.jxpath
* org.apache.commons.logging
* com.opensymphony.quartz (Not in BRMS ditribution)
* org.junit
* org.apache.xmlbeans


How to execute the script
=========================

Prerequisites
-------------

In order to execute this script some artifacts are required:

* The skinny BPMS/BRMS WAR distribution for Jboss EAP 6.1
* (Optional) The skinny jBPM Dashboard WAR distribution for Jboss EAP 6.1

Script arguemnts
----------------

The script is located in this directory and called <code>build-modules.sh</code>.

It takes some arguments:

1. Path to modules list file.
  This file contains the static modules to create. There are two possible files:
  - For BPMS --> [modules/bpms_modules.list](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/modules/bpms_modules.list)
  - For BRMS --> [modules/brms_modules.list](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/modules/brms_modules.list)
2. Path to JBoss EAP base modules list file.
  This file allows to support more EAP versions in the future.
  The version currently supported is 6.1.0.GA, so the file is: [modules/eap-6.1.0-modules.list](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/modules/eap-6.1.0-modules.list)
3. Webapp module name.
  Both BPMS/BRMS are considered as EAP dynamic modules and have the module definition and dependencies file for generating the final skinny WAR.
  The value for this arguments is:
   - For BPMS --> <code>kie-wb-webapp</code>
   - For BRMS --> <code>kie-drools-wb-webapp</code>
4. Path to BPMS/BRMS distribution WAR for EAP 6.1
  The skinny WAR for JBoss EAP 6.1 is generated in:
   - For BPMS --> [KIE WB Distribution WARS](https://github.com/droolsjbpm/kie-wb-distributions/tree/master/kie-drools-wb/kie-drools-wb-distribution-wars)
   - For BRMS --> [KIE Drools WB Distribution WARS](https://github.com/droolsjbpm/kie-wb-distributions/tree/master/kie-wb/kie-wb-distribution-wars)
5 Path to jBPM Dashboard WAR file - Optional
  If using BPMS skinny WAR as argument, you can add the jBPM Dashbuilder WAR to generate the skinny for new BPMS layer modules.
  It does not apply for BRMS, the BRMS distribution and jBPM Dashbuilder are not compatible.

Running the script
------------------
1. Open a terminal window, go to the <code>script/jbossmodules</code> directory and type the following command (for linux systems):
- For BPMS:
    $ sh build-modules.sh modules/bpms_modules.list modules/eap-6.1.0-modules.list "kie-wb-webapp" <PATH_TO_BPMS_WAR> <PATH_TO_JBPM_DASHBOARD_WAR>
- For BRMS:
    $ sh build-modules.sh modules/brms_modules.list modules/eap-6.1.0-modules.list "kie-drools-wb-webapp" <PATH_TO_BRMS_WAR>

  This command uncompress the WARs, extract the jars from the webapp and put them into a modules structure for JBoss EAP.

  This procedure generates a ZIP file in <code>dist</code> directory.

2. Once the zip file is generated into <code>dist</code> directory, go to your JBoss EAP home directory and uncompress it:

  $ cd $BJOSS_HOME
  $unzip -o <path_to_zip>.zip

  NOTE: Please use a clean EAP installation.
3. Once the application is started, open a browser and type the following URL:
    <code>http://localhost:8080/kie-wb</code>

Patches
=======
This script adds some patches to EAP that are required to run the application without errors.
These patches ara documented in [patches/README.md](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/patches/README.md)


