What does the script
====================

The script takes the kie-wb-eap-6_1.war from its source and restructures it to fit in a jBOSS EAP 6.1 application
creating modules and a war to deploy.
It copies also the **jbpm-dashbuilder-jboss-as7.war** from it's source for the later deployment.
The created modules are to be found under **/system/layers/bpms/...**
The **module.xml** are also automatically created.
The script creates a new war, **kie-wb-modules.war** that is much lighter than the original kie-wb-eap-6_1.war.

To assure the script executes correctly and that the jBOSS EAP 6.1 application runs with the copied modules and deployed
war-files some files are needed, you will find them under **/dependencies** or **/patches**.

Finally the sript creates a zip-file wich copies, when extracting it onto an jBOSS EAP 6.1 application the modules to
**<EAP 6.1 application>/modules/system/layers/bpms...**, and the two wars **kie-wb.war** (kie-wb-modules.war renamed)
and **jbpm-dashbuilder.war** (jbpm-dashbuilder-jboss-as7.war renamed) to **<EAP 6.1 application>/standalone/deployments **.
Also the file **layers.conf** is extracted to **<EAP 6.1 application>/modules/**.


How to execute the script
=========================

For starting the script the are only two parameters needed:

1. <path to EAP6.1 war file> : the path to the surce where it is located **kie-wb-eap-6_1.war**
2. <path to EAP 6.1 dashbuilder WAR file> : the path to the source where it is located **jbpm-dashbuilder-jboss-as7.war**

The script is to start with ./build-modules.sh <path to EAP6.1 war file> <path to EAP 6.1 dashbuilder WAR file>

The created zip file (**dist/bpms-modules.zip**) is ready to extract onto an jBOSS EAP 6.1 application.


