# 24 · Deployment: Docker, Kubernetes and CI/CD

> **Phase:** Advanced · **Week:** 12 · **Code:** [`Dockerfile`](../Dockerfile), [`docker-compose.yml`](../docker-compose.yml), [`k8s/bookstore.yaml`](../k8s/bookstore.yaml), [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

Code isn't done until it runs in production. This module packages the bookstore as a container,
runs it with PostgreSQL locally, describes a Kubernetes deployment, and automates build and test
on every push.

## Concepts

### The executable jar
**Level:** Beginner

`mvn package` builds `target/backend-zero-to-hero-1.0.0.jar` containing your code, all
dependencies and embedded Tomcat. Run it anywhere with Java: `java -jar target/*.jar`.

### Docker images
**Level:** Beginner · **Code:** [Dockerfile](../Dockerfile)

A multi-stage Dockerfile builds with a JDK image and runs on a smaller JRE image, as a non-root
user. Spring Boot's layered jars put dependencies (which rarely change) in a separate layer from
your classes, so rebuilds push only a few kilobytes. Alternatively, `mvn spring-boot:build-image`
uses Cloud Native Buildpacks with no Dockerfile at all.

### Docker Compose for local environments
**Level:** Beginner · **Code:** [docker-compose.yml](../docker-compose.yml)

`docker compose up --build` starts PostgreSQL and the app with the `postgres` profile. Add Redis,
Kafka, Prometheus and Grafana as you reach those modules. Boot's `spring-boot-docker-compose`
module can even start compose services when you run the app from your IDE.

### Twelve-factor configuration
**Level:** Intermediate

One build artifact, many environments: configuration comes from the environment
(`SPRING_PROFILES_ACTIVE`, `DB_URL`, `APP_JWT_SECRET`), logs go to stdout, processes are stateless and
disposable. Never bake secrets into images; use your platform's secret store.

### Kubernetes basics
**Level:** Intermediate · **Code:** [k8s/bookstore.yaml](../k8s/bookstore.yaml)

A Deployment keeps N replicas of a Pod running and rolls out new versions gradually. A Service gives
them a stable DNS name and load-balances across them. ConfigMaps and Secrets inject configuration.
Requests and limits reserve CPU and memory.

### Probes and graceful shutdown
**Level:** Intermediate · **Code:** [k8s/bookstore.yaml](../k8s/bookstore.yaml)

Point the readiness probe at `/actuator/health/readiness` (send traffic only when ready) and the
liveness probe at `/actuator/health/liveness` (restart if stuck). Graceful shutdown (`server.shutdown=graceful`, on by default since Boot 3.4) lets in-flight
requests finish when Kubernetes stops a pod during a rollout.

### CI/CD pipelines
**Level:** Intermediate · **Code:** [ci.yml](../.github/workflows/ci.yml)

Continuous integration builds and tests every push and pull request (GitHub Actions here).
Continuous delivery then builds the image, scans it, pushes it to a registry, and deploys to
staging and production, ideally with automated checks and a quick rollback path. Keep the main
branch always deployable.

### Native images and fast startup
**Level:** Advanced

GraalVM native images (`mvn -Pnative native:compile`) start in tens of milliseconds and use less
memory, at the cost of long builds and restrictions on reflection. Useful for serverless and
scale-to-zero. Test thoroughly: some libraries need extra hints.

## Practice

### Run with PostgreSQL
**Level:** Beginner

Run `docker compose up --build`, then call the API on port 8080 and connect to the database with
`psql` to see the Flyway history table and your orders.

### Publish an image from CI
**Level:** Intermediate

Extend the GitHub Actions workflow to build the Docker image on pushes to `main` and push it to
GitHub Container Registry with the commit SHA as the tag.

### Deploy to a local cluster
**Level:** Advanced

Install kind or minikube, load your image, apply `k8s/bookstore.yaml` with a PostgreSQL deployment,
scale to three replicas, and run a rolling update while a load test runs. Check that no requests fail.

## Interview questions

- Why use a multi-stage Docker build?
- Liveness vs readiness probe?
- How should secrets reach a container?
- What makes a deployment zero-downtime?

## Further reading

- [Spring Boot: Container images](https://docs.spring.io/spring-boot/reference/packaging/container-images/index.html)
- [Kubernetes concepts](https://kubernetes.io/docs/concepts/)
- [The Twelve-Factor App](https://12factor.net/)
- [GitHub Actions documentation](https://docs.github.com/en/actions)
