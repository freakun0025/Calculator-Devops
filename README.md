# Calculator API - End-to-End CI/CD Pipeline

A small Spring Boot **Calculator REST API** wrapped in a fully automated DevOps pipeline:
**Git/GitHub → Jenkins → Docker → Docker Hub → Docker Compose**.
A `git push` to `main` automatically builds, tests, packages, containerizes, pushes and deploys the app with no manual steps.

> CSE 816 - Software Production Engineering, Mini-Project
> Kunal Mittal (IMT2023533)

| Item | Link |
|------|------|
| GitHub repository | https://github.com/freakun0025/Calculator-Devops |
| Docker Hub image | https://hub.docker.com/r/freakun/calculator-api |

---

## Technology Stack

| Area | Tool |
|------|------|
| Application | Java 17, Spring Boot 3.3.5 (Spring Web + Actuator), Maven (wrapper included) |
| Testing | JUnit 5, Spring MockMvc |
| Version control | Git, GitHub (webhook trigger) |
| CI/CD | Jenkins declarative pipeline (`Jenkinsfile`) |
| Containers | Docker (multi-stage build), Docker Hub |
| Deployment | Docker Compose: API + Nginx reverse proxy |
| Notifications | Jenkins email (Gmail SMTP) |

## Repository Structure

```
calculator-devops/
├── src/main/java/com/example/calculator_devops/
│   ├── CalculatorDevopsApplication.java   # Spring Boot entry point
│   ├── CalculatorController.java          # REST endpoints (/api/v1/*)
│   ├── CalculatorService.java             # Business logic
│   ├── CalculationResponse.java           # Success JSON body
│   ├── ErrorResponse.java                 # Error JSON body
│   └── GlobalExceptionHandler.java        # Validation -> HTTP 400
├── src/main/resources/application.properties
├── src/test/java/com/example/calculator_devops/
│   ├── CalculatorServiceTest.java         # JUnit 5 unit tests
│   ├── CalculatorControllerTest.java      # MockMvc web-layer tests
│   └── CalculatorDevopsApplicationTests.java  # Full-context integration tests
├── pom.xml
├── Dockerfile            # multi-stage build
├── .dockerignore
├── docker-compose.yml    # API + Nginx proxy
├── nginx.conf
├── Jenkinsfile           # declarative CI/CD pipeline
├── .gitignore
└── README.md
```

---

## API Reference

| Operation | Endpoint | Behaviour |
|-----------|----------|-----------|
| Add | `GET /api/v1/add?a=&b=` | Returns `a + b` |
| Subtract | `GET /api/v1/subtract?a=&b=` | Returns `a - b` |
| Multiply | `GET /api/v1/multiply?a=&b=` | Returns `a * b` |
| Divide | `GET /api/v1/divide?a=&b=` | Returns `a / b`; `b=0` gives HTTP 400 |
| Health | `GET /actuator/health` | Spring Actuator health probe (`UP`/`DOWN`) |

**Success response (HTTP 200)**
```json
{ "operation": "add", "a": 2.0, "b": 3.0, "result": 5.0 }
```

**Error response (HTTP 400)**: for division by zero, a missing parameter, a non-numeric value, or `NaN`/`Infinity`
```json
{
  "timestamp": "2026-10-25T14:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Division by zero is not allowed",
  "path": "/api/v1/divide"
}
```

---

## Build, Test and Run Locally

Prerequisites: JDK 17+ (Maven is provided through the `./mvnw` wrapper), Docker Desktop / Docker Engine with Compose v2.

```bash
# Build the executable JAR (skip tests)
./mvnw clean package -DskipTests

# Run all tests (JUnit XML reports are written to target/surefire-reports/)
./mvnw test

# Run the JAR directly
java -jar target/calculator-devops-0.0.1-SNAPSHOT.jar
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

### Run with Docker (single container)

```bash
docker build -t freakun/calculator-api:latest .
docker run -d -p 8080:8080 --name calculator freakun/calculator-api:latest
curl "http://localhost:8080/api/v1/multiply?a=4&b=3"
```

(Or pull the published image: `docker run -d -p 8080:8080 freakun/calculator-api:latest`.)

### Run the full stack with Docker Compose

```bash
docker compose up -d     # starts calculator-api + nginx proxy
docker compose ps        # both services should show (healthy)
docker compose down      # stop and remove the stack
```

The API is then reachable through Nginx on port **80**:

```bash
curl http://localhost/actuator/health
curl "http://localhost/api/v1/add?a=2&b=3"
curl "http://localhost/api/v1/subtract?a=10&b=4"
curl "http://localhost/api/v1/multiply?a=4&b=3"
curl "http://localhost/api/v1/divide?a=10&b=2"
curl -i "http://localhost/api/v1/divide?a=10&b=0"   # HTTP 400
curl -i "http://localhost/api/v1/add?a=abc&b=1"     # HTTP 400
```

---

## Containerization (Dockerfile)

- **Multi-stage:** stage 1 (`maven:3.9.4-eclipse-temurin-17`) builds the JAR; stage 2 (`eclipse-temurin:17-jre-alpine`) copies **only** the JAR.
- **Layer caching:** `pom.xml` is copied and dependencies are resolved (`dependency:go-offline`) before the source, so code changes don't re-download dependencies.
- **Security:** runs as the non-root user `calcuser`.
- **Explicit `EXPOSE 8080`**, a `HEALTHCHECK` on `/actuator/health`, and a `.dockerignore` that keeps the build context small.

## Deployment (Docker Compose)

`docker-compose.yml` brings up the whole stack on the bridge network `calc-net`:

| Service | Image | Purpose |
|---------|-------|---------|
| `calculator-api` | `freakun/calculator-api:${IMAGE_TAG:-latest}` | Spring Boot API on internal port 8080; health check on `/actuator/health`; `restart: unless-stopped`; environment variables `SPRING_PROFILES_ACTIVE`, `SERVER_PORT`, `JAVA_TOOL_OPTIONS` |
| `proxy` | `nginx:1.27-alpine` | Reverse proxy on host port 80 that forwards to `calculator-api:8080`; starts only once the API is healthy |

Only the proxy is published on the host, so the API doesn't clash with Jenkins on port 8080. Nginx re-resolves the API container through Docker DNS, so redeploys don't leave it pointing at a stale IP.

---

## CI/CD Pipeline (Jenkinsfile)

```
 git push ──► GitHub ──webhook──► Jenkins
                                    │
   Checkout ─► Build ─► Test ─► Docker Build ─► Push ─► Deploy ─► Email
                       (JUnit)   (tag: build#)  (Docker Hub)  (compose up -d
                                                               + health check)
```

| Stage | What it does |
|-------|--------------|
| **Checkout** | `checkout scm`: pulls the commit that triggered the build |
| **Build** | `./mvnw clean package -DskipTests`: compiles and packages the JAR |
| **Test** | `./mvnw test`; publishes `target/surefire-reports/*.xml` with the `junit` step; the build fails if any test fails |
| **Docker Build** | `docker build` tagged `freakun/calculator-api:<BUILD_NUMBER>` and `:latest` |
| **Push** | Logs in to Docker Hub with the Jenkins credential `docker-hub-credentials` (`withCredentials`), pushes both tags, then logs out |
| **Deploy** | `docker compose up -d --remove-orphans` with `IMAGE_TAG=<BUILD_NUMBER>`, then polls `http://localhost/actuator/health` until it reports `UP` (fails the build otherwise) |
| **Post** | Emails a success or failure notification with the build link |

**Idempotent and re-runnable:** Compose only recreates containers whose image or config changed, the stack uses a fixed project name, and `disableConcurrentBuilds()` prevents two deployments from racing. Re-running a build leaves a running deployment working.

**No secrets in the repository:** the Docker Hub token is stored only in the Jenkins Credentials store, and the SMTP password only in Jenkins' email configuration.

### Jenkins setup (one-time)

1. **Plugins:** Pipeline, Git, GitHub, JUnit, Credentials Binding, Mailer.
2. **Credentials:** *Manage Jenkins → Credentials*, add a *Username with password* entry with ID `docker-hub-credentials` (Docker Hub username + access token).
3. **Email:** *Manage Jenkins → System → E-mail Notification*: SMTP server `smtp.gmail.com`, port 465 (SSL), using a Gmail app password.
4. **Job:** a *Pipeline* job, *Pipeline script from SCM*, Git URL of this repository, branch `*/main`, script path `Jenkinsfile`. Tick *GitHub hook trigger for GITScm polling* (the Jenkinsfile also declares `githubPush()`).
5. **Windows host only:** the pipeline uses `sh` steps, so add Git Bash to Jenkins' PATH: *Manage Jenkins → System → Global properties → Environment variables*: `PATH+GIT = C:\Program Files\Git\bin`.

### GitHub webhook (local Jenkins via ngrok)

1. `ngrok http 8080`
2. GitHub repo → *Settings → Webhooks → Add webhook*. Payload URL `https://<ngrok-id>.ngrok-free.app/github-webhook/`, content type `application/json`, event *Just the push event*.
3. Push a commit: GitHub calls the webhook, and Jenkins runs the pipeline and redeploys automatically.

---

## Git Workflow

- Work is done on feature branches (e.g. `feature/calculator-api`, `feature/validation-and-pipeline-hardening`) and merged into `main` with merge commits / pull requests.
- Conventional, incremental commit messages (`feat:`, `test:`, `build:`, `ci:`, `deploy:`, `docs:`).
- `.gitignore` excludes `target/`, IDE files and secret files (`.env`, keys, keystores).
