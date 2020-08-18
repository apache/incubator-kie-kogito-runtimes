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
        timeout(time: 360, unit: 'MINUTES')
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
                    checkoutRepo("kogito-runtimes", "integration-tests")
                    checkoutRepo("kogito-apps")
                    checkoutRepo("kogito-examples")
                    checkoutRepo("kogito-examples", "kogito-examples-persistence")
                    checkoutRepo("kogito-examples", "kogito-examples-events")
                }
            }
        }
        stage('Build kogito-runtimes') {
            steps {
                // TODO enable tests
                mavenCleanInstall("kogito-runtimes", true, ["run-code-coverage"])
                runMaven("validate", "kogito-runtimes", true, ["sonarcloud-analysis"], "-e -nsu")
            }
        }
        stage('Build apps and examples') {
            parallel {
                stage('Build integration-tests with persistence') {
                    steps {
                        sh "mkdir -p .m2/repository && rsync -av --progress ~/.m2/repository .m2/repository"
                        mavenCleanInstall("integration-tests", false, ["persistence"], "-Dmaven.repo.local=.m2/repository/")
                    }
                }
                stage('Build kogito-apps') {
                    steps {
                        sh "mkdir -p .m2/repository && rsync -av --progress ~/.m2/repository .m2/repository"
                        mavenCleanInstall("kogito-apps", false, [], "-Dmaven.repo.local=.m2/repository/")
                    }
                }
                stage('Build kogito-examples') {
                    steps {
                        sh "mkdir -p .m2/repository && rsync -av --progress ~/.m2/repository .m2/repository"
                        mavenCleanInstall("kogito-examples", false, [], "-Dmaven.repo.local=.m2/repository/")
                    }
                }
                stage('Build kogito-examples with persistence') {
                    steps {
                        sh "mkdir -p .m2/repository && rsync -av --progress ~/.m2/repository .m2/repository"
                        mavenCleanInstall("kogito-examples-persistence", false, ["persistence"], "-Dmaven.repo.local=.m2/repository/")
                    }
                }
                stage('Build kogito-examples with events') {
                    steps {
                        sh "mkdir -p .m2/repository && rsync -av --progress ~/.m2/repository .m2/repository"
                        mavenCleanInstall("kogito-examples-events", false, ["events"], "-Dmaven.repo.local=.m2/repository/")
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