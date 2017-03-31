def pushReleaseBranches =
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_pushReleaseBranches.sh
"""

def deployLocally=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_deployLocally.sh
"""

def copyToNexus=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_copyToNexus.sh
"""

def jbpmTestCoverageMatrix=
"""
git clone https://github.com/kiegroup/droolsjbpm-build-bootstrap.git -b master
sh \$WORKSPACE/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_jbpmTestCoverMartix.sh
"""

def kieAllServerMatrix=
"""
git clone https://github.com/kiegroup/droolsjbpm-build-bootstrap.git -b master
sh \$WORKSPACE/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_allServerMatrix.sh
"""

def kieWbSmokeTestsMatrix=
"""
git clone https://github.com/kiegroup/droolsjbpm-build-bootstrap.git -b master
sh \$WORKSPACE/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_wbSmokeTestsMatrix.sh
"""

def pushTags=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_pushTag.sh
"""

def removeBranches=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_removeReleaseBranches.sh
"""

def updateVersions=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_updateToNextDevelopmentVersion.sh
"""

def copyBinariesToFilemgmt=
"""
sh /home/jenkins/workspace/KIE-Release-7.0.x/release-scripts/droolsjbpm-build-bootstrap/script/release/DSL-scripts/KIE_copyBinariesToFilemgmt.sh
"""

// **************************************************************************

job("KIE-pushReleaseBranches-7.0.x") {

  description("This job: <br> checksout the right source- upgrades the version in poms <br> - modifies the kie-parent-metadata pom <br> - pushes the generated release branches to kiegroup <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
  
  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community: <b> community </b><br>or<br> if it is for building a productization tag: <b>productized <br> ******************************************************** <br> ")
    choiceParam("SOURCE", ["community-branch", "community-tag", "production-tag"], " please select the source of this release <br> or it is the master branch ( <b> community-branch </b> ) <br> or a community tag ( <b> community-tag </b> ) <br> or a productization tag ( <b> production-tag </b> ) <br> ******************************************************** <br> ")
    stringParam("TAG", "tag", "if you selected as <b> SOURCE=community-tag </b> or <b> SOURCE=production-tag </b> please edit the name of the tag <br> if selected as <b> SOURCE=community-branch </b> the parameter <b> TAG </b> will be ignored <br> The tag should typically look like <b> 7.0.0.CR1 </b> for <b> community </b> or <b> sync-7.0.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("RELEASE_VERSION", "release version", "please edit the version for this release <br> The <b> RELEASE_VERSION </b> should typically look like <b> 7.0.0.CR1 </b> for <b> community </b> or <b> 6.5.1.20160805-productization </b> for <b> productization </b> <br>******************************************************** <br> ")
    stringParam("BASE_BRANCH", "base branch", "please select the base branch <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "release branch", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-7.0.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("UBERFIRE_VERSION", "uberfire version", "please edit the right version to use of uberfire/uberfire-extensions <br> The tag should typically look like <b> 1.0.0.CR1 </b> for <b> community </b> or <b> 6.5.1.20170122-productization </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("DASHBUILDER_VERSION", "dashbuilder version", "please edit the right version to use of dashbuilder <br> The tag should typically look like <b> 0.6.0.CR1 </b> for <b> community </b> or <b> 6.5.1.20170122-productization </b> for <b> productization </b> <br> ******************************************************** <br> ") 
    stringParam("ERRAI_VERSION", "errai version", " please select the errai version<br> ******************************************************** <br> ")
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

job("KIE-buildDeployLocally-7.0.x") {

  description("This job: <br> - builds all repositories and deploys them locally <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
  
  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "release branch", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-6.5.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
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

job("KIE-copyToNexus-7.0.x") {

  description("This job: <br> - copies binaries from local dir to Nexus <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community: <b> community </b> or <br> if it is for building a productization tag: <b>productized <br> ******************************************************** <br> ")
  };

  label("kie-releases")
  
  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")
  
  customWorkspace("\$HOME/workspace/KIE-buildDeployLocally-7.0.x")

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
      trigger("KIE-allJbpmTestCoverageMatrix-7.0.x, KIE-AllServerMatrix-7.0.x, KIE-WbSmokeTestsMatrix-7.0.x") {
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

matrixJob("KIE-allJbpmTestCoverageMatrix-7.0.x") {

  description("This job: <br> - Test coverage Matrix for jbpm <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community: </b> or <br> if it is for building a productization tag: <b>productized <br> Version to test. Will be supplied by the parent job. <br> ******************************************************** <br> ")
    stringParam("KIE_VERSION", "KIE version", "please edit the version of the KIE release <br> i.e. typically <b> 7.0.0.CR1 </b> for <b> community </b>or <b> 6.5.1.20160805-productized </b> for <b> productization </b> <br> Version to test. Will be supplied by the parent job. <br> ******************************************************** <br> ")
  };

  axes {
    labelExpression("label-exp","linux && mem4g")
    jdk("jdk1.8")
  }

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
   }

  publishers {
    archiveJunit("**/TEST-*.xml")
    mailer('mbiarnes@redhat.com', false, false)
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
    shell(jbpmTestCoverageMatrix)
    maven{
      mavenInstallation("apache-maven-3.2.5")
      goals("clean verify -e -B -Dmaven.test.failure.ignore=true -Dintegration-tests")
      rootPOM("jbpm-test-coverage/pom.xml")
      mavenOpts("-Xmx3g")
      providedSettings("org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1438340407905")
    }  
  }  
}

// **********************************************************************************

matrixJob("KIE-AllServerMatrix-7.0.x") {
  description("This job: <br> - Runs the KIE Server integration tests on mutiple supported containers and JDKs <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated. ")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "<br> ******************************************************** <br> ")
    stringParam("KIE_VERSION", "KIE version", "the KIE_VERSION will be supplied by parent job")
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
      mavenSettings("org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1438340407905"){
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
      providedSettings("org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1438340407905")
    }  
  }  
}

// ****************************************************************************************************

matrixJob("KIE-WbSmokeTestsMatrix-7.0.x") {
  description("This job: <br> - Runs the smoke tests on KIE <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated. ")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "<br> ******************************************************** <br> ")
    stringParam("KIE_VERSION", "kie version", "the KIE_VERSION will be supplied by parent job")
  };
  
  axes {
    jdk("jdk1.8")
    text("container", "wildfly10", "tomcat8", "eap7")
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
      providedSettings("org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1438340407905")
    }  
  }  
}

// ************************************************************************************************

job("KIE-pushTags-7.0.x") {

  description("This job: <br> creates and pushes the tags for <br> community (kiegroup) or product (jboss-integration) <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community: <b> community </b> or <br> if it is for building a productization tag: <b>productized <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "release branch", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-6.5.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("TAG_NAME", "tag", "Please enter the tag. The tag should typically look like <b> 7.0.0.CR1 </b> for <b> community </b> or <b> sync-6.5.x-2017.01.22 </b> for <b> productization </b> <br> ******************************************************** <br> ")
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
    preBuildCleanup()
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

job("KIE-removeReleaseBranches-7.0.x") {

  description("This job: <br> creates and pushes the tags for <br> community (kiegroup) or product (jboss-integration) <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community: <b> community </b> or <br> if it is for building a productization tag: <b>productized <br> ******************************************************** <br> ")
    stringParam("BASE_BRANCH", "base branch", "please select the base branch <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "release branch", "please edit the name of the release branch <br> i.e. typically <b> r7.0.0.CR1 </b> for <b> community </b>or <b> bsync-6.5.x-2017.01.22  </b> for <b> productization </b> <br> ******************************************************** <br> ")
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
    preBuildCleanup()
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

job("KIE-updateToNextDevelopmentVersion-7.0.x") {

  description("This job: <br> updates the KIE repositories to a new developmenmt version <br> for 6.4.x, 6.5.x or 7.0.x branches <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
 
  parameters {
    stringParam("BASE_BRANCH","master","Branch you want to upgrade")
    stringParam("newVersion", "new KIE version", "Edit the KIE development version")
    stringParam("UF_DEVEL_VERSION", "uberfire version", "Edit the uberfire development version")
    stringParam("DASHB_DEVEL_VERSION", "dashbuilder version", "Edit the dashbuilder development version")
    stringParam("ERRAI_DEVEL_VERSION", "errai version", "Edit the errai development version")
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
    preBuildCleanup()
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

job("KIE-copyBinariesToFilemgmt-7.0.x") {

  description("This job: <br> copies kiegroup binaries to filemgmt.jbosss.org  <br> IMPORTANT: makes only sense for community releases <br><b> Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.<b>")

   parameters{
     stringParam("VERSION", "release version", "Edit the version of release, i.e. 7.0.0.Final")
   }

  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  customWorkspace("\$HOME/workspace/KIE-buildDeployLocally-7.0.x")

  wrappers {
    timeout {
      absolute(90)
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
    shell(copyBinariesToFilemgmt)
  }
}

// *****
// *****

listView("7.0.x-KIE-releases") {
  description("all needed scripts for builing a release of 7.0.x branch")
  jobs {
       name("KIE-pushReleaseBranches-7.0.x")
       name("KIE-buildDeployLocally-7.0.x")
       name("KIE-copyToNexus-7.0.x")
       name("KIE-allJbpmTestCoverageMatrix-7.0.x")
       name("KIE-AllServerMatrix-7.0.x")
       name("KIE-WbSmokeTestsMatrix-7.0.x")
       name("KIE-pushTags-7.0.x")
       name("KIE-removeReleaseBranches-7.0.x")
       name("KIE-updateToNextDevelopmentVersion-7.0.x")
       name("KIE-copyBinariesToFilemgmt-7.0.x")
  }
  columns {
       status()
       weather()
       name()
       lastSuccess()
       lastFailure()
  }
}
