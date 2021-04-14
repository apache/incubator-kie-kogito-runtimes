@Library('jenkins-pipeline-shared-libraries')_

import org.kie.jenkins.MavenCommand

changeAuthor = env.ghprbPullAuthorLogin ?: CHANGE_AUTHOR
changeBranch = env.ghprbSourceBranch ?: CHANGE_BRANCH
changeTarget = env.ghprbTargetBranch ?: CHANGE_TARGET

quarkusRepo = 'quarkus'
kogitoRuntimesRepo = 'kogito-runtimes'

pipeline {
    agent {
        label 'kie-rhel7 && kie-mem16g'
    }
    tools {
        maven 'kie-maven-3.6.2'
        jdk 'kie-jdk11'
    }
    options {
        timestamps()
        timeout(time: getTimeoutValue(), unit: 'MINUTES')
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

                    checkoutRepo(kogitoRuntimesRepo)
                }
            }
        }
        stage('Build quarkus') {
            when {
                expression { return getQuarkusBranch() }
            }
            steps {
                script {
                    checkoutQuarkusRepo()
                    runQuickBuild(quarkusRepo)
                }
            }
        }
        stage('Runtimes Build&Test') {
            steps {
                script {
                    if (isNormalPRCheck()) {
                        runUnitTests({ mvnCmd -> mvnCmd.withProperty('validate-formatting').withProfiles(['run-code-coverage']) })
                        runSonarcloudAnalysis()
                    } else {
                        runUnitTests(saveReports)
                    }
                }
            }
        }
        stage('Runtimes integration-tests') {
            steps {
                script {
                    runIntegrationTests()
                }
            }
        }
        stage('Runtimes integration-tests with persistence') {
            steps {
                script {
                    runIntegrationTests(['persistence'])
                }
            }
        }
    }
    post {
        always {
            script {
                sh '$WORKSPACE/trace.sh'
            }
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
        cleanup {
            script {
                util.cleanNode('docker')
            }
        }
    }
}

void checkoutRepo(String repo) {
    checkoutRepo(repo, changeAuthor, changeBranch, changeTarget)
}

void checkoutQuarkusRepo() {
    dir('quarkus') {
        checkout(githubscm.resolveRepository('quarkus', 'quarkusio', getQuarkusBranch(), false))
    }
}

MavenCommand getMavenCommand(String directory, boolean addQuarkusVersion=true, boolean canNative = true) {
    mvnCmd = new MavenCommand(this, ['-fae'])
                .withSettingsXmlId('kogito_release_settings')
                .withSnapshotsDisabledInSettings()
                .withProperty('java.net.preferIPv4Stack', true)
                .inDirectory(directory)
    if (addQuarkusVersion && getQuarkusBranch()) {
        mvnCmd.withProperty('version.io.quarkus', '999-SNAPSHOT')
    }
    if (canNative && isNative()) {
        mvnCmd.withProfiles(['native'])
            .withProperty('quarkus.native.container-build', true)
            .withProperty('quarkus.native.container-runtime', 'docker')
            .withProperty('quarkus.profile', 'native') // Added due to https://github.com/quarkusio/quarkus/issues/13341
    }
    return mvnCmd
}

void saveReports() {
    junit '**/target/surefire-reports/**/*.xml,**/target/failsafe-reports/**/*.xml'
}

void cleanContainers() {
    cloud.cleanContainersAndImages('docker')
}

String getQuarkusBranch() {
    return env['QUARKUS_BRANCH']
}

boolean isNative() {
    return env['NATIVE'] && env['NATIVE'].toBoolean()
}

boolean isDownstreamJob() {
    return env['DOWNSTREAM_BUILD'] && env['DOWNSTREAM_BUILD'].toBoolean()
}

String getUpstreamTriggerProject() {
    return env['UPSTREAM_TRIGGER_PROJECT']
}

boolean isNormalPRCheck() {
    return !(isDownstreamJob() || getQuarkusBranch() || isNative())
}

Integer getTimeoutValue() {
    return isNative() ? 600 : 240
}

void runQuickBuild(String project) {
    getMavenCommand(project, false, false)
            .withProperty('quickly')
            .run('clean install')
}

void runUnitTests(Closure alterMvnCmd = null) {
    def mvnCmd = getMavenCommand(kogitoRuntimesRepo)
                    .withProperty('quickTests')

    if (alterMvnCmd) {
        alterMvnCmd(mvnCmd)
    }

    runMavenTests(mvnCmd, 'clean install')
}

void runSonarcloudAnalysis(Closure alterMvnCmd = null) {
    def mvnCmd = getMavenCommand(kogitoRuntimesRepo)
        .withOptions(['-e', '-nsu'])
        .withProfiles(['sonarcloud-analysis'])

    if (alterMvnCmd) {
        alterMvnCmd(mvnCmd)
    }

    mvnCmd.run('validate')
}

void runIntegrationTests(List profiles=[], Closure alterMvnCmd = null) {
    String profileSuffix = profiles ? "-${profiles.join('-')}" : ''
    String itFolder = "${kogitoRuntimesRepo}-it${profileSuffix}"
    sh "cp -r ${kogitoRuntimesRepo} ${itFolder}"

    def mvnCmd = getMavenCommand(itFolder)

    if (alterMvnCmd) {
        alterMvnCmd(mvnCmd)
    }

    runMavenTests(mvnCmd.withProfiles(profiles), 'verify')
}

void runMavenTests(MavenCommand mvnCmd, String mvnRunCmd) {
    try {
        mvnCmd.run(mvnRunCmd)
    } catch (err) {
        throw err
    } finally {
        saveReports()
        cleanContainers()
    }
}
