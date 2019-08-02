def get_offenderapi_version() {
    sh '''
    #!/bin/bash +x
    grep "version = " build.gradle | awk '{print $3}' | sed 's/\"//g' > offenderapi.version
    '''
    return readFile("./offenderapi.version")
}

pipeline {
    agent { label "jenkins_slave" }

    environment {
        docker_image = "hmpps/new-tech-api"
        aws_region = 'eu-west-2'
        ecr_repo = ''
        OFFENDERAPI_VERSION = get_offenderapi_version()
    }

    options { 
        disableConcurrentBuilds() 
    }

    stages {
        stage ('Notify build started') {
            steps {
                slackSend(message: "Build Started - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL.replace('http://', 'https://').replace(':8080', '')}|Open>)")
            }
        }

        stage ('Initialize') {
            steps {
                sh '''
                    #!/bin/bash +x
                    echo "PATH = ${PATH}"
                    echo "OFFENDERAPI_VERSION = ${OFFENDERAPI_VERSION}"
                '''
            }
        }

       stage('Verify Prerequisites') {
           steps {
               sh '''
                    #!/bin/bash +x
                    echo "Testing AWS Connectivity and Credentials"
                    aws sts get-caller-identity
               '''
           }
       }

        stage('Gradle Dependencies') {
            steps {
                    sh '''
                        #!/bin/bash +x
                        make gradle-dependencies offenderapi_version=${OFFENDERAPI_VERSION};
                    '''
            }
        }

        stage('Gradle Tests') {
                    // Known issue with gradle tests stalling on the DeliusOffenderAPITest test suite
                    options {
                        timeout(time: 20, unit: 'MINUTES') 
                    }
                    steps {
                            sh '''
                                #!/bin/bash +x
                                make gradle-test offenderapi_version=${OFFENDERAPI_VERSION};
                            '''
                    }
            }

        stage('Gradle Assemble') {
            steps {
                    sh '''
                        #!/bin/bash +x
                        make gradle-assemble offenderapi_version=${OFFENDERAPI_VERSION};
                    '''
            }
       }

        stage('Get ECR Login') {
            steps {
                sh '''
                    #!/bin/bash +x
                    make ecr-login
                '''
                // Stash the ecr repo to save a repeat aws api call
                stash includes: 'ecr.repo', name: 'ecr.repo'
            }
        }
        stage('Build Docker image') {
           steps {
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make build offenderapi_version=${OFFENDERAPI_VERSION}
                '''
            }
        }
        stage('Image Tests') {
            steps {
                // Run dgoss tests
                sh '''
                    #!/bin/bash +x
                    make test
                '''
            }
        }
        stage('Push image') {
            steps{
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make push offenderapi_version=${OFFENDERAPI_VERSION}
                '''
                
            }            
        }
        stage ('Remove untagged ECR images') {
            steps{
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make clean-remote
                '''
            }
        }
        stage('Remove Unused docker image') {
            steps{
                unstash 'ecr.repo'
                sh '''
                    #!/bin/bash +x
                    make clean-local OFFENDERAPI_VERSION=${OFFENDERAPI_VERSION}
                '''
            }
        }
    }
    post {
        always {
            // Add a sleep to allow docker step to fully release file locks on failed run
            sleep(time: 3, unit: "SECONDS")
            deleteDir()
        }
        success {
            slackSend(message: "Build successful -${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL.replace('http://', 'https://').replace(':8080', '')}|Open>)", color: 'good')
        }
        failure {
            slackSend(message: "Build failed - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL.replace('http://', 'https://').replace(':8080', '')}|Open>)", color: 'danger')
        }
    }
}
