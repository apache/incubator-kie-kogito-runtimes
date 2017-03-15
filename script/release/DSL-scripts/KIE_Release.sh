def pushReleaseBranches =
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/01.KIE_pushReleaseBranches.sh
"""

def deployLocally=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/02.KIE_deployLocally.sh
"""

def copyToNexus=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/03.KIE_copyToNexus.sh
"""

def jbpmTestCoverageMatrix=
"""
git clone https://github.com/droolsjbpm/droolsjbpm-build-bootstrap.git -b master
sh \$WORKSPACE/droolsjbpm-build-bootstrap/script/release/DSL-scripts/04a.KIE_jbpmTestCoverMartix.sh
"""

def kieAllServerMatrix=
"""
git clone https://github.com/droolsjbpm/droolsjbpm-build-bootstrap.git -b master
sh \$WORKSPACE/droolsjbpm-build-bootstrap/script/release/DSL-scripts/04c.KIE_kieAllServerMatrix.sh
"""

def kieWbSmokeTestsMatrix=
"""
git clone https://github.com/droolsjbpm/droolsjbpm-build-bootstrap.git -b master
sh \$WORKSPACE/droolsjbpm-build-bootstrap/script/release/DSL-scripts/04d.KIE_kieWbSmokeTestsMatrix.sh
"""

def pushTags=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/05.KIE_pushTag.sh
"""

def removeBranches=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/06.KIE_removeReleaseBranches.sh
"""

def updateVersions=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/07.KIE_updateToNextDevelopmentVersion.sh
"""

def copyBinariesToFilemgmt=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/08.KIE_copyBinariesToFilemgmt.sh
"""

// **************************************************************************

job("01.pushReleaseBranches-7.0.x") {

  description("This job: <br> checksout the right source- upgrades the version in poms <br> - modifies the kie-parent-metadata pom <br> - pushes the generated release branches to droolsjbpm <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
  
  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
    choiceParam("SOURCE", ["community-branch", "community-tag", "production-tag"], " please select the source of this release <br> or it is the master branch ( <b> community-branch </b> ) <br> or a community tag ( <b> community-tag </b> ) <br> or a productization tag ( <b> production-tag </b> ) <br> ******************************************************** <br> ")
    stringParam("TAG", "7.0.0.CR1", "if you selected as <b> SOURCE=community-tag </b> or <b> SOURCE=production-tag </b> please edit the name of the tag <br> if selected as <b> SOURCE=community-branch </b> the parameter <b> TAG </b> will be ignored <br> The tag should typically look like <b> 7.0.0.CR1 </b> for <b> community </b> or <b> sync-7.0.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("RELEASE_VERSION", "7.0.0.CR1", "please edit the version for this release <br> The <b> RELEASE_VERSION </b> should typically look like <b> 7.0.0.CR1 </b> for <b> community </b> or <b> 6.5.0.20160805-productization </b> for <b> productization </b> <br>******************************************************** <br> ")
    choiceParam("BASE_BRANCH", ["master","7.0.x"], "please select the base branch <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "r7.0.0.CR1", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-7.0.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("UBERFIRE_VERSION", "1.0.0.CR1", "please edit the right version to use of uberfire/uberfire-extensions <br> The tag should typically look like <b> 1.0.0.CR1 </b> for <b> community </b> or <b> 1.0.0.20170122-productization </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("DASHBUILDER_VERSION", "0.6.0.CR1", "please edit the right version to use of dashbuilder <br> The tag should typically look like <b> 0.6.0.CR1 </b> for <b> community </b> or <b> 0.6.0.20170122-productization </b> for <b> productization </b> <br> ******************************************************** <br> ") 
    choiceParam("ERRAI_VERSION", ["4.0.0.Beta1","4.0.0.Beta2","4.0.0.Beta3","4.0.0.Beta4","4.0.0.Beta5","4.0.0.Beta6","4.0.0.Beta7"], " please select the errai version<br> ******************************************************** <br> ")
  };
  
  label("kie-releases")

  logRotator {
    numToKeep(10)
  }
 
  jdk("jdk1.8") 
  
  wrappers {
    timestamps()
    colorizeOutput()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }  
  
  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }
 
  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }    
    shell(pushReleaseBranches)
  }
}

// **************************************************************************************

job("02.buildDeployLocally-7.0.x") {

  description("This job: <br> - builds all repositories and deploys them locally <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
  
  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "r7.0.0.CR1", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-7.0.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
  };
  
  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8") 
  
  publishers {
    archiveJunit("**/TEST-*.xml")
  }

  wrappers {
    timestamps()
    colorizeOutput()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }  

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }  
 
  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }    
    shell(deployLocally)
  }
}

// ********************************************************************************

job("03.copyToNexus-7.0.x") {

  description("This job: <br> - copies binaries from local dir to Nexus <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
  };

  label("kie-releases")
  
  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")
  
  customWorkspace("\$HOME/workspace/02.buildDeployLocally-7.0.x")

  wrappers {
    timestamps()
    colorizeOutput()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers{
    downstreamParameterized {
      trigger("04a.allJbpmTestCoverageMatrix, 04b.kieAllServerMatrix, 04c.kieWbSmokeTestsMatrix") {
        condition("SUCCESS")
        parameters {
          propertiesFile("kie.properties", true)
        }
      }
    }
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(copyToNexus)
  }


}

// **************************************************************************************

matrixJob("04a.allJbpmTestCoverageMatrix-7.0.x") {

  description("This job: <br> - Test coverage Matrix for jbpm <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> Version to test. Will be supplied by the parent job. <br> ******************************************************** <br> ")
    stringParam("KIE_VERSION", "7.0.0.CR1", "please edit the version of the KIE release <br> i.e. typically <b> 7.0.0.CR1 </b> for <b> community </b>or <b> 6.5.0.20160805-productized </b> for <b> productization </b> <br> Version to test. Will be supplied by the parent job. <br> ******************************************************** <br> ")
  };

  axes {
    jdk("jdk1.8")
  }              

  logRotator {
    numToKeep(10)
  }

  label("linux && mem4g")
  
  wrappers {
    timestamps()
    colorizeOutput()
    preBuildCleanup()
   }

  publishers {
    archiveJunit("**/TEST-*.xml")
    mailer('mbiarnes@redhat.com', false, false)
  }
  
  steps {
    shell(jbpmTestCoverageMatrix)
    maven{
      mavenInstallation("apache-maven-3.2.5")
      goals("clean verify -e -B -Dmaven.test.failure.ignore=true -Dintegration-tests")
      rootPOM("jbpm-test-coverage/pom.xml")
      mavenOpts("-Xmx3g")
      providedSettings("settings-consume-internal-kie-builds")
    }  
  }  
}

// **********************************************************************************

matrixJob("04b.kieAllServerMatrix-7.0.x") {
  description("This job: <br> - Runs the KIE Server integration tests on mutiple supported containers and JDKs <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated. ")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "<br> ******************************************************** <br> ")
    stringParam("KIE_VERSION", "7.0.0.CR1", "the KIE_VERSION will be supplied by parent job")
  };
  
  axes {
    jdk("jdk1.8")
    text("container", "tomcat8", "wildfly10")
    labelExpression("label_exp", "linux && mem4g")
  }              
  
  childCustomWorkspace("\${SHORT_COMBINATION}")

  logRotator {
    numToKeep(10)
  }
  
  wrappers {
    timeout {
      absolute(120)
    }
    timestamps()
    colorizeOutput()
    preBuildCleanup()
    configFiles {
      mavenSettings("settings-consume-internal-kie-builds"){
        variable("SETTINGS_XML_FILE")      
      }  
    }    
   }
  

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    archiveJunit("**/target/failsafe-reports/TEST-*.xml")
    mailer('mbiarnes@redhat.com', false, false)
  }
  
  steps {
    shell(kieAllServerMatrix)
    maven{
      mavenInstallation("apache-maven-3.2.5")
      goals("-B -U -e -fae clean verify -P\$container")
      rootPOM("kie-server-parent/kie-server-tests/pom.xml")
      properties("kie.server.testing.kjars.build.settings.xml":"\$SETTINGS_XML_FILE")
      properties("maven.test.failure.ignore": true)
      properties("deployment.timeout.millis":"240000")
      properties("container.startstop.timeout.millis":"240000")
      properties("eap64x.download.url":"http://download.devel.redhat.com/released/JBEAP-6/6.4.4/jboss-eap-6.4.4-full-build.zip")
      mavenOpts("-Xms1024m -Xmx1536m")
      providedSettings("settings-consume-internal-kie-builds")
    }  
  }  
}

// ****************************************************************************************************

matrixJob("04c.kieWbSmokeTestsMatrix-7.0.x") {
  description("This job: <br> - Runs the smoke tests on KIE <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated. ")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "<br> ******************************************************** <br> ")
    stringParam("KIE_VERSION", "7.0.0.CR1", "the KIE_VERSION will be supplied by parent job")
  };
  
  axes {
    jdk("jdk1.8")
    text("container", "wildfly10")
    text("war", "kie-wb", "kie-drools-wb")
    labelExpression("label_exp", "linux && mem4g && gui-testing")
  }              
  
  childCustomWorkspace("\${SHORT_COMBINATION}")
  
  properties {
    rebuild {
      autoRebuild()
    }
  } 

  logRotator {
    numToKeep(10)
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }
  
  wrappers {
    timeout {
      absolute(120)
    }
    timestamps()
    colorizeOutput()
    preBuildCleanup()
    xvnc {
      useXauthority(true)
    }  
   }

  publishers {
    archiveJunit("**/target/failsafe-reports/TEST-*.xml")
    mailer('mbiarnes@redhat.com', false, false)
  }
  
  steps {
    shell(kieWbSmokeTestsMatrix)
    maven{
      mavenInstallation("apache-maven-3.2.5")
      goals("-B -e -fae clean verify -P\$container,\$war,selenium -D\$TARGET")
      rootPOM("kie-wb-tests/pom.xml")
      properties("maven.test.failure.ignore":true)
      properties("deployment.timeout.millis":"240000")
      properties("container.startstop.timeout.millis":"240000")
      properties("webdriver.firefox.bin":"/opt/tools/firefox-38esr/firefox-bin")
      properties("eap7.download.url":"http://download.eng.brq.redhat.com/released/JBEAP-7/7.0.2/jboss-eap-7.0.2-full-build.zip")
      mavenOpts("-Xms1024m -Xmx1536m")
      providedSettings("settings-consume-internal-kie-builds")
    }  
  }  
}

// ************************************************************************************************

job("05.pushTags-7.0.x") {

  description("This job: <br> creates and pushes the tags for <br> community (droolsjbpm) or product (jboss-integration) <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "r7.0.0.CR1", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-7.0.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("TAG_NAME", "Please enter the name of the tag", "The tag should typically look like <b> 7.0.0.CR1 </b> for <b> community </b> or <b> sync-7.0.x-2017.01.22 </b> for <b> productization </b> <br> ******************************************************** <br> ")
  };

  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  wrappers {
    timeout {
      absolute(30)
    }
    timestamps()
    colorizeOutput()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    mailer('mbiarnes@redhat.com', false, false)
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(pushTags)
  }
}

// ***********************************************************************************

job("06.removeReleaseBranches-7.0.x") {

  description("This job: <br> creates and pushes the tags for <br> community (droolsjbpm) or product (jboss-integration) <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
    choiceParam("BASE_BRANCH", ["master","7.0.x"], "please select the base branch <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "r7.0.0.CR1", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-7.0.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
  };

  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  wrappers {
    timeout {
      absolute(30)
    }
    timestamps()
    colorizeOutput()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    mailer('mbiarnes@redhat.com', false, false)
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(removeBranches)
  }
}

// ****************************************************************************************

job("07.updateToNextDevelopmentVersion-7.0.x") {

  description("This job: <br> updates the KIE repositories to a new developmenmt version <br> for 6.4.x, 6.5.x or 7.0.x branches <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
 
  parameters {
    stringParam("newVersion", "7.0.1-SNAPSHOT", "Edit the next KIE development version")
    stringParam("UBERFIRE_VERSION", "1.0.1-SNAPSHOT", "Edit the next uberfire development version")
    stringParam("DASHBUILDER_VERSION", "0.6.1-SNAPSHOT", "Edit the next dashbuilder development version")
    stringParam("ERRAI_VERSION", "4.0.1-SNAPSHOT", "Edit the next errai development version")
  }

  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  wrappers {
    timeout {
      absolute(30)
    }
    timestamps()
    colorizeOutput()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    mailer('mbiarnes@redhat.com', false, false)
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(updateVersions)
  }
}

// ****************************************************************************************

job("08.copyBinariesToFilemgmt-7.0.x") {

  description("This job: <br> copies droolsjbpm binaries to filemgmt.jbosss.org  <br> IMPORTANT: makes only sense for community releases <br><b> Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.<b>")

  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  customWorkspace("\$HOME/workspace/02.buildDeployLocally-7.0.x")

  wrappers {
    timeout {
      absolute(30)
    }
    timestamps()
    colorizeOutput()
    preBuildCleanup()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    mailer('mbiarnes@redhat.com', false, false)
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(copyBinariesToFilemgmt)
  }
}

// *****
// *****

nestedView("7.0.x-Releases") {
    views {
        listView("KIE-7.0.x") {
            jobs {
                name("01.pushReleaseBranches-7.0.x")
                name("02.buildDeployLocally-7.0.x")
                name("03.copyToNexus-7.0.x")
                name("04a.allJbpmTestCoverageMatrix-7.0.x")
                name("04b.kieAllServerMatrix-7.0.x")
                name("04c.kieWbSmokeTestsMatrix-7.0.x")
                name("05.pushTags-7.0.x")
                name("06.removeReleaseBranches-7.0.x")
                name("07.updateToNextDevelopmentVersion-7.0.x")
                name("08.copyBinariesToFilemgmt-7.0.x")
            }
            columns {
                status()
                weather()
                name()
                lastSuccess()
                lastFailure()
            }
        }
    }
}
