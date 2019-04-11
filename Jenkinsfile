def submarineBomScmCustom = null
def submarineExamplesScmCustom = null

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
            subject: "Build $BRANCH_NAME failed",
            body: "Build $BRANCH_NAME failed! For more information see $BUILD_URL",
            recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
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
//                echo "PATH = ${PATH}"
//                echo "M2_HOME = ${M2_HOME}"
//                echo "Original branch: $CHANGE_BRANCH"
//                echo "Target branch: $CHANGE_TARGET"
//                echo "PR author: $CHANGE_AUTHOR_EMAIL"
                sh 'printenv'
                script {
                    try {
                        submarineBomScmCustom = resolveRepository('submarine-bom', "$CHANGE_AUTHOR", "$CHANGE_BRANCH", true)
                    } catch (Exception ex) {
                        echo "Branch $CHANGE_BRANCH from repository submarine-bom not found in $CHANGE_AUTHOR organisation."
                    }

                    try {
                        submarineExamplesScmCustom = resolveRepository('submarine-examples', "$CHANGE_AUTHOR", "$CHANGE_BRANCH", true)
                    } catch (Exception ex) {
                        echo "Branch $CHANGE_BRANCH from repository submarine-examples not found in $CHANGE_AUTHOR organisation."
                    }
                }
            }
        }
        stage('Build submarine-bom') {
            when {
                expression {
                    return submarineBomScmCustom != null
                }
            }
            steps {
                timeout(15) {
                    dir("submarine-bom") {
                        checkout submarineBomScmCustom
                        sh 'mvn clean install -DskipTests'
                    }
                }
            }
        }
        stage('Build submarine-runtimes') {
            steps {
                timeout(30) {
                    sh 'mvn clean install'
                }
            }
        }
        stage('Build submarine-examples') {
            steps {
                timeout(30) {
                    dir("submarine-examples") {
                        script {
                            if (submarineExamplesScmCustom != null) {
                                checkout submarineExamplesScmCustom
                            } else {
                                checkout(resolveRepository('submarine-examples', 'kiegroup', "$CHANGE_TARGET", false))
                            }
                        }
                        sh 'mvn clean install'
                    }
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
            cleanWs()
        }
    }
}