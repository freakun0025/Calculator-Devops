pipeline {
    agent any

    // Triggered by the GitHub webhook (POST to <jenkins>/github-webhook/)
    triggers {
        githubPush()
    }

    options {
        skipDefaultCheckout(true)      // explicit Checkout stage below
        disableConcurrentBuilds()      // two deploys must never race each other
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        DOCKER_IMAGE = 'freakun/calculator-api'
        IMAGE_TAG    = "${BUILD_NUMBER}"
        NOTIFY_EMAIL = 'mittalkunal0025@gmail.com'
        HEALTH_URL   = 'http://localhost/actuator/health'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                // A failing test makes mvn exit non-zero, which fails the build
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} -t ${DOCKER_IMAGE}:latest ."
            }
        }

        stage('Push') {
            steps {
                // Docker Hub login comes from the Jenkins Credentials store, never from the repo
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh "docker push ${DOCKER_IMAGE}:${IMAGE_TAG}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                }
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }

        stage('Deploy') {
            steps {
                // Idempotent: compose only recreates containers whose config/image changed.
                // IMAGE_TAG pins the deployment to the image built in this run.
                sh 'docker compose up -d --remove-orphans'
                sh '''
                    echo "Waiting for $HEALTH_URL to report UP..."
                    for i in $(seq 1 30); do
                        if curl -fs "$HEALTH_URL" | grep -q '"status":"UP"'; then
                            echo "Health check passed (attempt $i)"
                            docker compose ps
                            exit 0
                        fi
                        sleep 5
                    done
                    echo "Deployment did not become healthy in time"
                    docker compose ps
                    docker compose logs --tail=50
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            mail to: "${NOTIFY_EMAIL}",
                 subject: "SUCCESS: ${JOB_NAME} #${BUILD_NUMBER}",
                 body: """The Calculator API pipeline succeeded.

Image deployed: ${DOCKER_IMAGE}:${IMAGE_TAG}
Health: ${HEALTH_URL}
Build details: ${BUILD_URL}"""
        }
        failure {
            mail to: "${NOTIFY_EMAIL}",
                 subject: "FAILED: ${JOB_NAME} #${BUILD_NUMBER}",
                 body: """The Calculator API pipeline failed.

Check the console output: ${BUILD_URL}console"""
        }
    }
}
