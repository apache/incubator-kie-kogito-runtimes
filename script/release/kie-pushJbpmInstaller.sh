#!/bin/bash -e

jbpmHtdocs=jbpm@filemgmt.jboss.org:/downloads_htdocs/jbpm/release

uploadInstaller(){
        # upload installers to filemgmt.jboss.org
        scp jbpm-installer-$version.zip $jbpmHtdocs/$version
}

uploadAllInstaller(){
        # upload installers to filemgmt.jboss.org
        scp jbpm-installer-$version.zip $jbpmHtdocs/$version
        # upload installers to filemgmt.jboss.org
        scp jbpm-installer-full-version.zip $jbpmHtdocs/$version
}

if [[ $version == *"Final"* ]] ;then
        uploadAllInstaller
else
        uploadInstaller
fi

