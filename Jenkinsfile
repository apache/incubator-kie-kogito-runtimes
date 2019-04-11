def submarineBomScm = null
def submarineExamplesScm = null

def resolveRepository(String repository, String author, String branches, boolean ignoreErrors) {
    return resolveScm(
            source: github(
                    credentialsId: 'kie-ci',
                    repoOwner: author,
                    repository: repository,
                    traits: [[$class: 'org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait', strategyId: 1],
                             [$class: 'org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait', strategyId: 1],
                             [$class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait', strategyId: 1, trust: [$class: 'TrustPermission']]]),
            ignoreErrors: ignoreErrors,
            targets: [branches])
}

def sendEmailFailure() {
    emailext (
            subject: "Build for PR $BRANCH_NAME failed",
            body: "Build for PR $BRANCH_NAME failed! For more infformation see $BUILD_URL",
            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}

pipeline {
    agent {
//        label 'kie-rhel7'
        label 'submarine-static'
    }
    tools {
        maven 'kie-maven-3.5.4'
        jdk 'kie-jdk1.8'
    }
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')
    }
    stages {
        stage('Initialize') {
            steps {
                echo "PATH = ${PATH}"
                echo "M2_HOME = ${M2_HOME}"
                echo "Original branch: $CHANGE_BRANCH"
                echo "Target branch: $CHANGE_TARGET"
                echo "PR author: $CHANGE_AUTHOR_EMAIL"
                script {
                    try {
                        submarineBomScm = resolveRepository('submarine-bom', "$CHANGE_AUTHOR", "$CHANGE_BRANCH", true)
                    } catch (Exception ex) {
                        echo "Branch $CHANGE_BRANCH from repository submarine-bom not found in $CHANGE_AUTHOR organisation."
                    }

                    try {
                        submarineExamplesScm = resolveRepository('submarine-examples', "$CHANGE_AUTHOR", "$CHANGE_BRANCH", true)
                    } catch (Exception ex) {
                        echo "Branch $CHANGE_BRANCH from repository submarine-examples not found in $CHANGE_AUTHOR organisation."
                    }
                }
            }
        }
        stage('Build submarine-bom') {
            when {
                expression {
                    return submarineBomScm != null
                }
            }
            steps {
                dir("submarine-bom") {
                    checkout submarineBomScm
                    sh 'mvn clean install -DskipTests'
                }
            }
        }
        stage('Build submarine-runtimes') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Build submarine-examples') {
            steps {
                dir("submarine-examples") {
                    script {
                        if (submarineExamplesScm != null) {
                            checkout submarineExamplesScm
                        } else {
                            checkout(resolveRepository('submarine-examples', 'kiegroup', "$CHANGE_TARGET", false))
                        }
                    }
                    sh 'mvn clean install'
                }
            }
        }
        stage('Publish test results') {
            steps {
                junit '**/target/surefire-reports/**/*.xml'
            }
        }
    }
    post {
        unstable {
            script {
                sendEmailFailure()
            }
        }
        failure {
            script {
                sendEmailFailure()
            }
        }
        always {
            sendEmailFailure()
            cleanWs()
        }
    }
}