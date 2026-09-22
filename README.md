# Distributed Job Processing System

A high-throughput, fault-tolerant **Distributed Job Processing System** built using **Java 17**, **Spring Boot 3**, **PostgreSQL**, **Redis**, and **Docker**.

Designed for executing computational jobs asynchronously across concurrent worker pools with priority-based scheduling, exponential retries, lock leasing, crash recovery, and database persistence.

---

## Key Technical Features

* **REST API Layer**: REST endpoints for job submission, status polling, cancellation, job listing, and system operational metrics.
* **Priority Scheduling**: Redis Sorted Set (`ZSET`) priority queue (with in-memory `PriorityBlockingQueue` fallback). Calculates dynamic scores based on `JobPriority` (HIGH: 300, MEDIUM: 200, LOW: 100) and FIFO submission timestamps.
* **Concurrent Worker Pools**: Java `ExecutorService` managing worker thread pools with active capacity tracking and polling loop dispatching.
* **State Persistence & Resilience**: Persistent state stored in database (PostgreSQL in containerized mode; H2 in-memory for zero-dependency local runs). Jobs survive application restarts.
* **Distributed Locks & Lease Renewal**: `DistributedLockService` (Redis `SETNX` with TTL, backed by local lock fallback) prevents duplicate job executions across scaled worker nodes.
* **Exponential Retries & Fault Tolerance**: Automated retries with exponential backoff (`2^attempt * 1000ms`) for transient failures up to `maxRetries`.
* **Orphan Job Recovery**: `OrphanJobRecoveryScheduler` periodically identifies stale jobs stranded in `RUNNING` status without an active worker heartbeat and re-queues them.
* **Live Interactive Dashboard**: Embedded web UI (`http://localhost:8080`) providing live metrics (worker utilization, queue counts, job status breakdown), job submission form, preset batch generators, and real-time execution tracking.
* **Docker Containerization**: `Dockerfile` and `docker-compose.yml` for single-command deployment of multi-node app instances, PostgreSQL, and Redis.

---

## Tech Stack

* **Language**: Java 17 (Eclipse Temurin)
* **Framework**: Spring Boot 3.2.3 (Spring Data JPA, Spring Data Redis, Spring Web)
* **Databases**: PostgreSQL / H2 Database
* **Cache & Priority Queue**: Redis 7 (Lettuce client)
* **Containerization**: Docker & Docker Compose
* **Build System**: Apache Maven / Maven Wrapper (`mvnw.cmd`)

---

## Project Structure

```
distributed-job-processor/
├── src/main/java/com/jobprocessor/
│   ├── DistributedJobProcessorApplication.java
│   ├── controller/
│   │   ├── JobController.java          # REST API (/api/v1/jobs)
│   │   └── MetricsController.java      # Operational Metrics (/api/v1/metrics)
│   ├── domain/
│   │   ├── JobEntity.java              # JPA Entity
│   │   ├── JobPriority.java            # Enum (HIGH, MEDIUM, LOW)
│   │   └── JobStatus.java              # Enum (PENDING, QUEUED, RUNNING, COMPLETED, FAILED, RETRYING, CANCELLED)
│   ├── dto/
│   │   ├── SubmitJobRequest.java
│   │   ├── JobResponse.java
│   │   └── SystemMetricsResponse.java
│   ├── repository/
│   │   └── JobRepository.java          # Spring Data JPA Repo
│   ├── scheduler/
│   │   └── OrphanJobRecoveryScheduler.java # Crash recovery scanner
│   ├── service/
│   │   ├── JobExecutionService.java    # Computational workloads & simulations
│   │   ├── JobService.java             # Orchestration service
│   │   ├── PriorityQueueService.java   # Redis ZSET / Priority queue
│   │   └── DistributedLockService.java # Distributed locking mechanism
│   └── worker/
│       └── WorkerPoolManager.java      # ExecutorService worker pool controller
├── src/main/resources/
│   ├── static/index.html              # Interactive Dashboard UI
│   └── application.yml                 # Configuration profiles
├── Dockerfile                              # Multi-stage Docker build
├── docker-compose.yml                      # Postgres + Redis + Scalable App
└── pom.xml                                 # Maven project file
```

---

## Quick Start (Local Standalone Mode)

Requires only **Java 17** installed. Out-of-the-box in-memory fallback enables running without installing external Postgres or Redis!

```bash
# Compile and build
.\mvnw.cmd compile

# Run unit & integration tests
.\mvnw.cmd test

# Launch the application
java -Dmaven.multiModuleProjectDirectory="%CD%" -cp .mvn/wrapper/maven-wrapper.jar org.apache.maven.wrapper.MavenWrapperMain spring-boot:run
```

Once running, open your browser to:
* **Interactive Dashboard**: `http://localhost:8080`
* **H2 Database Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:jobdb`)

---

## Containerized Deployment (Docker Compose)

To spin up PostgreSQL, Redis, and the Job Processor application in containerized mode:

```bash
docker compose up --build -d
```

To scale worker nodes horizontally:
```bash
docker compose up --scale job-processor-node=3 -d
```

---

## REST API Overview

### 1. Submit a Job
`POST /api/v1/jobs`
```json
{
  "name": "Fibonacci Computation",
  "jobType": "CALCULATION",
  "priority": "HIGH",
  "payload": "35",
  "maxRetries": 3
}
```

### 2. Get Job Status & Result
`GET /api/v1/jobs/{id}`

### 3. List Jobs
`GET /api/v1/jobs?status=RUNNING`

### 4. Cancel a Job
`POST /api/v1/jobs/{id}/cancel`

### 5. System Metrics
`GET /api/v1/metrics`
```json
{
  "workerNodeId": "worker-node-de3774d9",
  "activeWorkerCount": 2,
  "poolSize": 4,
  "queueSize": 5,
  "jobCountsByStatus": {
    "COMPLETED": 12,
    "QUEUED": 5,
    "RUNNING": 2,
    "FAILED": 0
  }
}
```
