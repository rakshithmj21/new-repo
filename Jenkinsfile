 // Jenkinsfile (Declarative)
pipeline {
  agent {
    // Use an agent that has docker and kubectl installed.
    // If you run with Kubernetes plugin, use an image with docker client + kubectl,
    // or run on a node with docker and kubectl pre-installed.
    label 'docker'
  }

  environment {
    // Edit these names to match your Jenkins credentials and desired image
    REGISTRY = "cdocker"                      // or your private registry
    REPO     = "rakshi21/cdocker"             // change
    IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    FULL_IMAGE = "${REGISTRY}/${REPO}:${IMAGE_TAG}"
    DOCKER_CREDENTIALS = 'docker-hub-creds'     // Jenkins credential ID (username:password)
    KUBECONFIG_CREDENTIAL = 'kubeconfig-file'   // Jenkins "Secret file" credential containing kubeconfig
    K8S_MANIFESTS = "k8s"                       // path to manifests
  }

  options {
    skipStagesAfterUnstable()
    timestamps()
  }

  stages {
    stage('Checkout') {
 steps {
        checkout scm
        script { echo "Checked out ${env.GIT_COMMIT}" }
      }
    }

    stage('Build Docker image') {
      steps {
        sh 'docker --version || true'
        // Build image
        sh "docker build -t ${FULL_IMAGE} ."
      }
    }

    stage('Push to registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDENTIALS, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "$DOCKER_PASS" | docker login ${REGISTRY} -u "$DOCKER_USER" --password-stdin
            docker push ${FULL_IMAGE}
            docker logout ${REGISTRY}
          '''
        }
      }
    }
 stage('Deploy to Kubernetes') {
      steps {
        // Write kubeconfig to workspace and use it for kubectl
        withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIAL, variable: 'KUBECONFIG_FILE')]) {
          sh '''
            mkdir -p $HOME/.kube
            cp ${KUBECONFIG_FILE} $HOME/.kube/config
            export KUBECONFIG=$HOME/.kube/config

            # Replace image tag inside k8s manifests (simple sed). Supports single-file and manifests dir.
            find ${K8S_MANIFESTS} -type f -name "*.yaml" -print0 | while IFS= read -r -d '' f; do
              echo "Updating image refs in $f"
              # backup
              cp "$f" "$f.bak"
              sed -i "s#YOUR_REGISTRY/YOUR_REPOSITORY/myapp:\${IMAGE_TAG}#${FULL_IMAGE}#g" "$f"
            done

            kubectl apply -f ${K8S_MANIFESTS}
            kubectl rollout status deployment/myapp --namespace=default --timeout=120s || true

            # show resources for pipeline logs
            kubectl get all -o wide
          '''
        }
      }
 }

    stage('Smoke Test') {
      steps {
        // optional: call a simple health endpoint to verify app
        // curl -fsS http://<service-ip-or-ingress>/ || echo "smoke test failed"
        echo 'Smoke test step (optional) - add commands suitable for your app'
      }
    }
  }

  post {
    success {
      echo "Pipeline succeeded: ${FULL_IMAGE}"
    }
    failure {
      echo "Pipeline failed"
    }
  }
}
