def prepare_env() {
    sh '''
    #!/usr/env/bin bash
    docker pull mojdigitalstudio/hmpps-oraclejdk-builder:latest
    '''
}

pipeline {

    agent { label "jenkins_slave" }

    stages {

        stage('Setup') {
            steps {
                git url: 'git@github.com:noms-digital-studio/delius-offender-api.git', branch: 'master', credentialsId: 'f44bc5f1-30bd-4ab9-ad61-cc32caf1562a'
                prepare_env()
            }
        }

        stage('Build') {
            steps {
                sh '''
                    docker run --rm -v `pwd`:/home/tools/data mojdigitalstudio/hmpps-oraclejdk-builder bash -c "./gradlew dependencies"
                    docker run --rm -v `pwd`:/home/tools/data mojdigitalstudio/hmpps-oraclejdk-builder bash -c "./gradlew assemble"
                '''
            }
        }

        stage ('Package') {
            steps {
                sh 'docker build -t 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-api:latest --file ./Dockerfile .'
                sh 'aws ecr get-login --no-include-email --region eu-west-2 | source /dev/stdin'
                sh 'docker push 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-api:latest'
            }
        }

        stage('trigger deployment') {
            steps {
                build job: 'New_Tech/Deploy_Delius_API', parameters: [[$class: 'StringParameterValue', name: 'environment_type', value: 'dev']]
            }
        }

    }

    post {
        always {
            deleteDir()
        }
    }

}
