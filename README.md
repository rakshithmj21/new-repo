

---

# Kubernetes + Jenkins + SonarQube CI/CD Pipeline

## Overview

This project demonstrates a **full CI/CD workflow** for a sample Java application using **Jenkins**, **SonarQube**, **Docker**, and **Kubernetes**.

The workflow includes:

1. Building a Java application.
2. Performing **static code analysis** with SonarQube.
3. Building a Docker image.
4. Pushing the Docker image to DockerHub.
5. Deploying the app to a **Kubernetes cluster**.
6. Verifying the deployment and troubleshooting.

---

## Architecture

```
Developer -> GitHub -> Jenkins -> DockerHub -> Kubernetes Cluster
                                 |
                                 v
                            SonarQube Analysis
```

* **Jenkins**: CI/CD orchestrator.
* **SonarQube**: Static code analysis server.
* **Docker**: Containerization of the app.
* **Kubernetes**: Deployment of app and SonarQube server.
* **GitHub**: Version control and SCM.

---

## Cluster Setup

1. **Control Plane (CP) and Worker Nodes**:

   * Deployed on **EC2 instances**.
   * CP runs `kube-apiserver`, `kube-controller-manager`, `kube-scheduler`, `etcd`.
   * Workers run `kubelet`, `containerd`, `kube-proxy`.
   * **Flannel** is used for networking (CNI).
2. **Important**:

   * Free-tier EC2 instances use **dynamic private IPs**.
   * If nodes are stopped and restarted, **private IPs may change**, requiring:

     * Updating `kubeconfig` with new CP IP.
     * Rejoining workers with `kubeadm join` if they fail to connect.

---

## Jenkins Setup

1. Install Jenkins on a dedicated EC2 node.
2. Configure credentials:

   * **DockerHub**: Username and password.
   * **Kubeconfig**: For Kubernetes access from Jenkins pipelines.
3. Plugins installed:

   * **Pipeline**: For declarative pipelines.
   * **Git**: Source control.
   * **Docker Pipeline**: Build and push Docker images.
   * **SonarQube Scanner**: Integrate static code analysis.

---

## Sample Application

* **Language**: Java
* **Build Tool**: Maven
* **Dockerized** using:

```dockerfile
FROM openjdk:17
COPY target/app.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
EXPOSE 8080
```

* **Important Fix**: Make sure `app.jar` contains a **Main-Class** in `MANIFEST.MF` to avoid `CrashLoopBackOff`.

---

## SonarQube Setup

* **Deployed on Kubernetes** as `NodePort`.
* Default port for dashboard: `30090`.
* Steps:

  1. Deploy SonarQube Deployment and Service (`kubectl apply -f k8s/sonarqube.yaml`).
  2. Access dashboard: `http://<worker-ip>:30090`.
  3. Configure Jenkins with SonarQube token.
  4. Add **SonarQube Scanner** step in Jenkins pipeline.

---

## Jenkins Pipeline

**Key Stages**:

1. **Checkout**: Pull latest code from GitHub.
2. **SonarQube Analysis**: Scan code using `withSonarQubeEnv`.
3. **Build Docker Image**: Build image from `Dockerfile`.
4. **Push to DockerHub**: Login and push the image.
5. **Deploy to Kubernetes**: Apply `deployment.yaml` and `service.yaml`, update image.
6. **Verify Deployment**: Check pods and services.

**Key Notes**:

* Always use **unique Docker tags** (`build-1`, `build-2`) to avoid caching issues.
* Use `imagePullPolicy: Always` in Kubernetes to pull the latest image.
* If pods fail (`CrashLoopBackOff`), check logs using `kubectl logs <pod>`.

---

## Troubleshooting

| Issue                           | Cause                      | Fix                                                       |
| ------------------------------- | -------------------------- | --------------------------------------------------------- |
| `CrashLoopBackOff`              | JAR missing Main-Class     | Add Main-Class in `pom.xml` and rebuild                   |
| `CrashLoopBackOff`              | Wrong Docker image         | Tag new version and update deployment                     |
| `kubectl get nodes` fails       | Client credentials missing | Set `KUBECONFIG=/path/to/admin.conf` and update server IP |
| Pods not starting after restart | Worker cannot reach CP     | Re-run `kubeadm join` with new master IP                  |
| Old app version displayed       | Kubernetes cached image    | Use unique image tag and `kubectl set image`              |

---

## Useful Commands

* **Check pods**: `kubectl get pods -o wide`
* **Check services**: `kubectl get svc`
* **Update deployment image**:

```bash
kubectl set image deployment/<deployment> <container>=<image>:<tag> --record
kubectl rollout status deployment/<deployment> --timeout=120s
```

* **Logs**: `kubectl logs <pod>`
* **Rejoin worker**:

```bash
sudo kubeadm join <master-ip>:6443 --token <token> --discovery-token-ca-cert-hash sha256:<hash>
```

---

## References

* [Kubernetes Official Docs](https://kubernetes.io/docs/)
* [Jenkins Pipeline Docs](https://www.jenkins.io/doc/book/pipeline/)
* [SonarQube Docs](https://docs.sonarqube.org/)
* [Docker Docs](https://docs.docker.com/)

---

This README serves as a **future reference** for deploying CI/CD pipelines with Jenkins, Docker, and Kubernetes while using SonarQube for code quality analysis.

---


