@Library('jenkins-pipeline-shared-libraries')_

changeAuthor = env.ghprbPullAuthorLogin ?: CHANGE_AUTHOR
changeBranch = env.ghprbSourceBranch ?: CHANGE_BRANCH
changeTarget = env.ghprbTargetBranch ?: CHANGE_TARGET

pipeline {
    agent {
        label 'kie-rhel7 && kie-mem16g'
    }
    tools {
        maven 'kie-maven-3.6.2'
        jdk 'kie-jdk11'
    }
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '10', numToKeepStr: '')
        timeout(time: 120, unit: 'MINUTES')
    }
    environment {
        SONARCLOUD_TOKEN = credentials('SONARCLOUD_TOKEN')
        MAVEN_OPTS = '-Xms1024m -Xmx4g'
    }
    stages {
        stage('Initialize') {
            steps {
                script {
                    mailer.buildLogScriptPR()

                    checkoutRepo("kogito-runtimes")
                    checkoutRepo("kogito-apps")
                    checkoutRepo("kogito-examples")
                    checkoutRepo("kogito-examples", "kogito-examples-persistence")
                    checkoutRepo("kogito-examples", "kogito-examples-events")
                }
            }
        }
        stage('Prepare for testing') {
            steps {
                mavenCleanInstall("kogito-runtimes", true)
                // Remove kogito-apps once working on https://issues.redhat.com/browse/KOGITO-2720
                // as useless if examples do not depend on them
                mavenCleanInstall("kogito-apps", true)
                // No need to install examples for testing
                // mavenCleanInstall("kogito-examples", true)
            }
        }
        stage("Tests") {
            parallel {
                stage('Test Runtimes') {
                    steps {
                        mavenVerify("kogito-runtimes", ["run-code-coverage"])
                        runMaven("validate", "kogito-runtimes", false, ["sonarcloud-analysis"], "-e -nsu")
                    }
                }
                stage('Test Apps') {
                    steps {
                        mavenVerify("kogito-apps")
                    }
                }
                stage('Test Examples') {
                    steps {
                        mavenVerify("kogito-examples")
                    }
                }
                stage('Test Examples with persistence') {
                    steps {
                        mavenVerify("kogito-examples-persistence", ["persistence"])
                    }
                }
                stage('Test Examples with events') {
                    steps {
                        mavenVerify("kogito-examples-events", ["events"])
                    }
                }
            }
        }
    }
    post {
        always {
            sh '$WORKSPACE/trace.sh'
            junit '**/target/surefire-reports/**/*.xml, **/target/failsafe-reports/**/*.xml'
            cleanWs()
        }
        failure {
            script {
                mailer.sendEmail_failedPR()
            }
        }
        unstable {
            script {
                mailer.sendEmail_unstablePR()
            }
        }
        fixed {
            script {
                mailer.sendEmail_fixedPR()
            }
        }
    }
}

void checkoutRepo(String repo, String dirName=repo) {
    dir(dirName) {
        githubscm.checkoutIfExists(repo, changeAuthor, changeBranch, 'kiegroup', changeTarget, true)
    }
}

void mavenCleanInstall(String directory, boolean skipTests = false, List profiles = [], String extraArgs = "") {
    runMaven("clean install", directory, skipTests, profiles, extraArgs)
}

void mavenVerify(String directory, List profiles = [], String extraArgs = "") {
    runMaven("verify", directory, false, profiles, extraArgs)
}

void runMaven(String command, String directory, boolean skipTests = false, List profiles = [], String extraArgs = "") {
    mvnCmd = command
    if(profiles.size() > 0){
        mvnCmd += " -P${profiles.join(',')}"
    }
    if(extraArgs != ""){
        mvnCmd += " ${extraArgs}"
    }
    dir(directory) {
        maven.runMavenWithSubmarineSettings(mvnCmd, skipTests)
    }
}