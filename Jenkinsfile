pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('MySonarQube') {
          sh 'cd app && mvn clean verify sonar:sonar -Dsonar.projectKey=hello-world'
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "cd app && docker build -t ${DOCKERHUB_USER}/hello-world:latest ."
        sh "docker push ${DOCKERHUB_USER}/hello-world:latest"
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
          sh "kubectl apply -f k8s/"
        }
      }
    }

    stage('Verify') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
          sh "kubectl get pods -A"
          sh "kubectl get svc -A"
        }
      }
    }
  }
}
