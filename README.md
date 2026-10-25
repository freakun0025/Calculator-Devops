# Calculator API - Automated CI/CD Pipeline

This repository contains a Spring Boot-based Calculator API and a fully automated Continuous Integration and Continuous Deployment (CI/CD) pipeline. The project demonstrates modern DevOps practices, including automated building, testing, containerization, and deployment.

## 🛠️ Technology Stack
* **Application:** Java 17, Spring Boot 3.3.5, Maven
* **CI/CD Server:** Jenkins
* **Containerization:** Docker, Docker Compose
* **Reverse Proxy:** Nginx
* **Version Control & Automation:** Git, GitHub Webhooks, Ngrok (for local tunneling)
* **Notifications:** Gmail SMTP

## 🚀 Pipeline Architecture

The `Jenkinsfile` is written using declarative syntax and executes native Linux `sh` commands to maintain Ubuntu-environment compatibility, even when hosted on a Windows machine (achieved via Git Bash path injection). 

The pipeline consists of the following automated stages:
1. **Build:** Compiles the Java source code using the Maven wrapper (`./mvnw clean package`), skipping tests to isolate the build phase.
2. **Test:** Executes automated JUnit tests via `spring-boot-starter-test`.
3. **Docker Build:** Packages the compiled `.jar` artifact into a Docker image tagged with the specific Jenkins `BUILD_NUMBER` and `latest`.
4. **Push:** Securely authenticates with Docker Hub using Jenkins credentials and pushes the newly built images to the registry.
5. **Deploy:** Executes `docker compose up -d` to spin up the updated backend container alongside an Nginx reverse proxy.
6. **Post-Actions (Email):** Automatically sends a success or failure notification to the configured Gmail address containing the build status.

## 🏗️ Docker & Networking Setup
The deployment utilizes `docker-compose.yml` to orchestrate two services on an isolated bridge network (`calc-net`):
* **calculator-api:** Runs internally on port 8080. Includes health checks targeting the Spring Boot Actuator endpoint.
* **proxy (Nginx):** Listens on host port `80` and routes external traffic to the internal API container. This prevents port conflicts with the local Jenkins server running on port `8080`.

## ⚙️ Local Environment Configuration

### 1. Jenkins Environment
To execute the Linux-based `Jenkinsfile` on a Windows host, the Git Bash binary path is injected directly into Jenkins:
* **Global Properties:** `PATH+GIT` = `C:\Program Files\Git\bin`

### 2. GitHub Webhook Automation (via Ngrok)
To allow GitHub to trigger the local Jenkins server on every push:
1. Start an Ngrok tunnel: `ngrok http 8080`
2. Add the generated Forwarding URL to the GitHub Repository Webhooks, appending `/github-webhook/` to the endpoint.
3. Set the Content-Type to `application/json`.

## 🧪 Testing the Application
Once the pipeline deploys successfully, the application is accessible via the Nginx proxy on standard HTTP port 80.

**Check API Health:**
```bash
curl http://localhost/actuator/health
