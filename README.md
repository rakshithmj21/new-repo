---

# 🚀 CI/CD Pipeline: Java + Jenkins + SonarQube + Docker + Kubernetes

This repository demonstrates a **complete CI/CD setup**:

1. Build a Java **Hello World** application
2. Run **SonarQube analysis** (inside Kubernetes)
3. Build & push a **Docker image** to DockerHub
4. Deploy to **Kubernetes** via Jenkins
5. Verify the deployment

---

## 📂 Project Structure

```
k8s-sonarqube-demo/
├── Jenkinsfile
├── README.md
├── app/                 # Java app + Dockerfile
└── k8s/                 # Kubernetes manifests
    ├── deployment.yaml
    ├── service.yaml
    └── sonarqube.yaml
```

---

## ⚙️ Prerequisites

* **Infrastructure**

  * 1 EC2 (Ubuntu) → Jenkins server
  * 1 EC2 (control-plane) + 2 EC2 (workers) → Kubernetes cluster

* **Installed tools**

  * `docker`
  * `kubectl`
  * `maven`
  * `git`

* **Accounts**

  * DockerHub (for pushing images)
  * GitHub (for repository & webhook)

---

## 🔧 Step 1: Deploy SonarQube in Kubernetes

```bash
kubectl apply -f k8s/sonarqube.yaml
kubectl get pods -n sonarqube
kubectl get svc -n sonarqube
```

👉 SonarQube will be available at:

```
http://<worker-node-ip>:30090
```

* Login: `admin / admin`
* Reset password
* Create **SonarQube project** + generate **token**

---

## 🔧 Step 2: Configure Jenkins

### Install Plugins

* GitHub Integration
* Pipeline
* Docker
* SonarQube Scanner

### Add Jenkins Credentials

* **DockerHub** → username/password (`dockerhub-creds`)
* **SonarQube token** → secret text (`sonarqube-token`)
* **Kubeconfig** → upload as file credential (`kubeconfig`)

### Configure SonarQube in Jenkins

* Go to: `Manage Jenkins → Configure System`
* Add SonarQube server URL:

  ```
  http://<worker-node-ip>:30090
  ```
* Link with the `sonarqube-token`

---

## 🔑 Step 3: Authorize Jenkins to Access Kubernetes

1. From your control-plane node, copy the kubeconfig file:

   ```bash
   cat ~/.kube/config
   ```

2. Upload this as a **Jenkins File Credential** (`kubeconfig`).

3. In Jenkinsfile, reference it:

   ```groovy
   withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
       sh 'kubectl get nodes'
   }
   ```

👉 This ensures Jenkins can run `kubectl` commands against your cluster.

---

## 🔧 Step 4: GitHub Webhook

1. In your GitHub repo → `Settings → Webhooks → Add webhook`
2. Payload URL:

   ```
   http://<jenkins-server-ip>:8080/github-webhook/
   ```
3. Content type: `application/json`
4. Select: `Just the push event`

---

## 🔧 Step 5: Pipeline Workflow

The Jenkinsfile defines stages:

1. **Checkout** → Pulls latest code from GitHub
2. **Build Java App** → `mvn clean package`
3. **SonarQube Analysis** → Runs `mvn sonar:sonar` inside cluster
4. **Build & Push Docker Image** → Pushes to DockerHub
5. **Deploy to Kubernetes** → Applies manifests + updates image
6. **Verify Deployment** → Lists pods and services

---

## 🔧 Step 6: Verify Deployment

Check pod status:

```bash
kubectl get pods
```

Check service:

```bash
kubectl get svc hello-world-svc
```

Access app:

```bash
curl http://<worker-node-ip>:32085/
```

Expected output:

```
Hello World from Kubernetes!
```

---

## 🐛 Troubleshooting

* **Pod CrashLoopBackOff**

  * `kubectl logs <pod>` → usually caused by wrong JAR packaging

* **Service not reachable**

  * Use **worker node IP**, not control-plane
  * Check security group allows `nodePort`

* **SonarQube not reachable**

  * Ensure pod is running: `kubectl get pods -n sonarqube`
  * Verify NodePort `30090` is open

* **Jenkins cannot access Kubernetes**

  * Validate:

    ```bash
    withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
      sh 'kubectl get nodes'
    }
    ```

---

## ✅ End-to-End Flow

1. Developer pushes code → GitHub Webhook triggers Jenkins
2. Jenkins pipeline runs build → SonarQube → Docker → K8s deploy
3. App is exposed via NodePort → accessible from browser


