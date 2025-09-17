pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "arjunpdas/hello-world"
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
          withSonarQubeEnv('sonarqube-token') {
            sh 'mvn clean verify sonar:sonar'
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
        sh 'docker logout || true'
      }
    }
  }
}
