pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CRED = credentials('docker-hub-credentials')
        DOCKER_IMAGE = 'freakun/calculator-api'
    }
    
    stages {
        stage('Build') {
            steps {
                sh './mvnw clean package -Dmaven.test.skip=true'
            }
        }
        
        stage('Test') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} -t ${DOCKER_IMAGE}:latest ."
            }
        }
        
        stage('Push') {
            steps {
                sh "echo \$DOCKER_HUB_CRED_PSW | docker login -u \$DOCKER_HUB_CRED_USR --password-stdin"
                sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }
        
        stage('Deploy') {
            steps {
                sh 'docker compose up -d'
            }
        }
    }
    
    post {
        success {
            mail to: 'kunal.mittal@iiitb.ac.in',
                 subject: "Success: Jenkins Build ${BUILD_NUMBER}",
                 body: "The Calculator API deployed successfully."
        }
        failure {
            mail to: 'kunal.mittal@iiitb.ac.in',
                 subject: "Failed: Jenkins Build ${BUILD_NUMBER}",
                 body: "The Calculator API build failed. Check Jenkins logs."
        }
    }
}