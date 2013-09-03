Patches
=======

This directory contains some patches required to run BPMS/BRMS applications using a custom EAP static module layer distribution.

NOTE: These patches currently applies to version 6.1.0.GA. On next releases some of them are unnecessary.

EAP patches
===========

CDI Extensions
--------------
* In EAP 6.1.0.GA the CDI Extensions declared in JARs from the BPMS layer are not loaded.
* This bug is already reported and fixed for EAP 6.1.1.
   See https://bugzilla.redhat.com/show_bug.cgi?id=988093
* The patch consist of copying the resources from jars inside <code>META-INF/servicesc/code> directory of the webapp.
* The directory [patches/cdi-extensions](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/tree/master/script/jbossmodules/patches/cdi-extensions) contains all extension files.

Note that cdi extensions to patch differ from BPMS and BRMS.
* For BPMS -> The extensions to copy are the ones from property <code>module.patches.cdi-extensions</code> of file [modules/kie-wb-webapp.module](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/modules/kie-wb-webapp.module)
* For BRMS -> The extensions to copy are the ones from property <code>module.patches.cdi-extensions</code> of file [modules/kie-drools-wb-webapp.module](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/modules/kie-drools-wb-webapp.module)

Servlet spec 3.0 - Webfragments
-------------------------------
Is known that on both EAP 6.1.0. and 6.1.1 webfragment descriptors located inside custom static modules are not loaded.

So, the patch consist of copy the content of the webfragments from BPMS layer modules inside web deployment descriptor <code>WEB-INF/web.xml</code>.

Currently the patch does not override xml content, the <code>WEB-INF/web.xml</code> for BPMS/BRMS is copyied inside this directory.

Note Deployment descriptors to copy inside webapp differs from BPMS and BRMS
* For BPMS -> The web.xml to copy if the one from property <code>module.patches.web-xml</code> of file [modules/kie-wb-webapp.module](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/modules/kie-wb-webapp.module)
* For BRMS -> The web.xml to copy if the one from property <code>module.patches.web-xml</code> of file [modules/kie-drools-wb-webapp.module](https://github.com/droolsjbpm/droolsjbpm-build-bootstrap/blob/master/script/jbossmodules/modules/kie-drools-wb-webapp.module)

Seam transactions
-----------------
Seam consists of two artifacts:
* seam-transaction-api-3.X.jar
* seam-transapction-3.X.jar

The jBPM core static module for EAP depends on seam transaction api. So, this jars should be placed in another static module, not in the webapp.
But for a unknown reason yet, when putting seam-transaction-3.X.jar outside the webapp, the transactions are not running.
The reason seems to be that the transaction interceptor defined in <code>beans.xml</code> located inside webapp, is not registered if seam-transaction-3.X.jar (impl classes) is outside webapp lib.
This interceptor is:
 <code>
 <interceptors>
      <class>org.jboss.seam.transaction.TransactionInterceptor</class>
  </interceptors>
 </code>
This behaviour should be analyzed with EAP team.

REST services
-------------
As seam transactions, if the jar containing kie remote REST services <code>kie-common-services-6-X</code> is located outside webapp lib, for example inside a EAP static module, the services are not running.
This behaviour should be analyzed with EAP team.
