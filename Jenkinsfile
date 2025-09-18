/*pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "rakshithmj21/hello-world"
    DOCKER_TAG = "latest"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Maven Build & SonarQube Analysis') {
      steps {
        dir('app') {
          withSonarQubeEnv('MySonarQube') {   // <-- this is the SonarQube server name from Jenkins config
            withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
              sh """
                mvn clean package sonar:sonar \
                  -Dsonar.login=$SONAR_TOKEN
              """
            }
          }
        }
      }
    }

    stage('Build & Push Docker Image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerHub', usernameVariable: 'DH_USER', passwordVariable: 'DH_PWD')]) {
          sh '''
            echo $DH_PWD | docker login -u $DH_USER --password-stdin
            cd app
            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
          '''
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
          sh '''
            kubectl apply -f k8s/deployment.yaml
            kubectl apply -f k8s/service.yaml
            kubectl set image deployment/hello-world hello-world=${DOCKER_IMAGE}:${DOCKER_TAG} --record || true
            kubectl rollout status deployment/hello-world --timeout=120s
          '''
        }
      }
    }

    stage('Verify Deployment') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
          sh '''
            kubectl get pods -o wide
            kubectl get svc hello-world-svc -o wide
          '''
        }
      }
    }
  }

  post {
    always {
      script {
        // Logout Docker only if login was used
        try {
          sh 'docker logout'
        } catch (Exception e) {
          echo "Docker logout skipped."
        }
      }
    }
  }
}*/

pipeline {
  agent any

  triggers {
    // Trigger Jenkins pipeline via GitHub webhook
    githubPush()
  }

  environment {
    DOCKER_IMAGE = "arjunpdas/hello-world"
    DOCKER_TAG = "latest"   // unique tag per build
    //SONAR_TOKEN = credentials('sonarqube-token')                // SonarQube token credential
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    /*stage('SonarQube Analysis') {
      steps {
        dir('app') {
          withSonarQubeEnv('MySonarQube') {
            withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')])
            sh '''
              mvn clean verify sonar:sonar \
              -Dsonar.projectKey=hello-world \
              -Dsonar.login=$SONAR_TOKEN
            '''
          }
        }
      }
    }*/

    
    stage('Maven Build & SonarQube Analysis') {
      steps {
        dir('app') {
          withSonarQubeEnv('MySonarQube') {   // <-- this is the SonarQube server name from Jenkins config
            withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
              sh """
                mvn clean package sonar:sonar \
                  -Dsonar.login=$SONAR_TOKEN
              """
            }
          }
        }
      }
    }

    stage('Build & Push Docker Image') {
      when {
        expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerHub', usernameVariable: 'DH_USER', passwordVariable: 'DH_PWD')]) {
          sh '''
            echo $DH_PWD | docker login -u $DH_USER --password-stdin
            cd app
            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
            docker push ${DOCKER_IMAGE}:latest
          '''
        }
      }
    }

    stage('Deploy to Kubernetes') {
      when {
        expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
      }
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
          sh '''
            kubectl apply -f k8s/deployment.yaml
            kubectl apply -f k8s/service.yaml
            kubectl set image deployment/hello-world hello-world=${DOCKER_IMAGE}:${DOCKER_TAG} || true
            kubectl rollout status deployment/hello-world --timeout=120s
          '''
        }
      }
    }

    stage('Verify Deployment') {
      when {
        expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
      }
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
          sh '''
            kubectl get pods -o wide
            kubectl get svc hello-world-svc -o wide
          '''
        }
      }
    }
  }

  post {
    always {
      sh 'docker logout || true'
    }
  }
}

