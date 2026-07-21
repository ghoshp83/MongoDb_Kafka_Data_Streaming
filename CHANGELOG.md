# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Removed
- **The `api` package and the unreachable processing lineage hanging off it.**
  Two parallel sets of processing classes coexisted. The live one is rooted at
  `core.process.DocumentProcessingStrategy`; the other was rooted at
  `api.DocumentProcessingStrategy` and had no callers. Removed
  `api.DocumentProcessingStrategy`, `api.ChangeEventObserver`,
  `core.process.DocumentProcessor`, `core.process.BatchDocumentProcessor`,
  `core.process.StreamProcessor`, `core.process.ChangeStreamSubject`, and the
  duplicate `core.kafka.KafkaDocumentProcessor`.
- **Flat `core/` classes left behind by the subpackage migration.**
  `ChangeStreamProcessor`, `InitialLoader`, `KafkaFactory`,
  `LocalResumeTokenManager`, `MongoFactory`, `ResumeTokenManager`,
  `ResumeTokenManagerFactory`, and `S3ResumeTokenManager` had all been
  superseded by copies under `core/mongo` and `core/kafka`, which are what
  `ApplicationContext` wires up. Also removed the unused
  `core.kafka.KafkaFactory` and a stray non-test `KafkaDocumentProcessor.java`
  that had been committed under `src/test`.

### Fixed
- **`KafkaDocumentProcessorTest` tested the wrong class.** It imported the
  orphaned `core.kafka` copy while `ApplicationContext` constructs the
  `core.process` one, so the production processing path was uncovered. The two
  had diverged: the live implementation prefers the `vuid` field as the Kafka
  record key, wraps payloads in an `_operation`/`_source`/`_timestamp`
  envelope, and emits `documents.*` counters, where the orphan delegated to
  `DocumentConverter` and emitted `kafka.documents.*`. Repointed the suite and
  added coverage for the metadata envelope (68 tests, up from 67).
- Architecture docs described the pre-migration package layout and several
  deleted classes. Package tree, component diagram, and all code examples now
  match the source.

### Note
- `pom.xml` still declares `1.0.0` while this changelog records a `1.1.0`
  release; the version was not bumped at that release and is left untouched
  here.

## [1.1.0] - 2026-07-01

### Changed
- **Java 11 → 17**: Maven `source`/`target` bumped to 17; all seven GitHub
  Actions workflows and the multi-stage Dockerfile now build/run on Temurin 17.
- **Maven build plugins**: compiler 3.10.1→3.13.0, assembly 3.3.0→3.7.1,
  surefire/failsafe 2.22.2→3.5.2, JaCoCo 0.8.8→0.8.12 (JDK 17 class-file
  support).
- **Core runtime deps**: kafka-clients 3.3.1→3.9.0, jackson 2.13.3→2.18.2,
  aws-sdk s3 2.17.230→2.28.11, slf4j 1.7.36→2.0.16, logback 1.2.11→1.5.3,
  lombok 1.18.24→1.18.36.
- **Test deps**: junit-jupiter 5.8.2→5.11.4, mockito 4.5.1→5.10.0,
  assertj 3.22.0→3.27.7.

### Removed
- Unused `spring-boot-starter`, `spring-boot-starter-actuator`,
  `testcontainers`, `testcontainers:kafka`, and `de.flapdoodle.embed.mongo`
  dependencies (zero references in `src/`; health checks use the in-house
  `HealthCheckServer`, all 67 tests are Mockito unit tests). Cuts a large
  transitive tree and its CVE surface.
- `mockito-inline` — the inline mock-maker is the default in Mockito 5, so
  `mockStatic`/`mockConstruction` now work via `mockito-core`.

## [1.0.0] - 2026-04-17

### Changed
- **BREAKING:** Renamed root package `com.enterprise.department` → `com.github.ghoshp83.mongokafkastream`. Maven groupId moved from `com.enterprise.department` to `com.github.ghoshp83`. Update any downstream imports and Maven coordinates accordingly.
- Cut first stable release; feature surface (resumable CDC, S3 resume tokens, circuit breaker, graceful shutdown, health endpoints) is production-ready.

### Added
- `docker-compose.quickstart.yml` at the repo root: spins up MongoDB, Kafka, Zookeeper, a Kafka UI, and the streamer itself, plus an auto-seed script. `docker compose -f docker-compose.quickstart.yml up` and you're streaming in under 60 seconds.
- Enhanced GitHub repository presentation with improved badges and documentation
- Contributing guidelines and community files
- Comprehensive changelog documentation

## [0.1.0] - 2024-11-17

### Added
- **Core Features**
  - Initial data loading from MongoDB collections to Kafka
  - Real-time Change Data Capture (CDC) using MongoDB Change Streams
  - Resumable processing with resume token storage
  - Support for both local and AWS S3 resume token storage
  - Batch processing for optimal network utilization
  - Horizontal scaling support for multiple instances

- **Architecture & Design**
  - Modular, component-based architecture
  - Implementation of multiple design patterns (Strategy, Observer, Factory, Circuit Breaker)
  - Clean separation of concerns across packages
  - Dependency injection for component management

- **Resilience & Reliability**
  - Circuit breaker pattern for external service failures
  - Configurable retry mechanisms with exponential backoff
  - Graceful shutdown handling
  - Comprehensive error handling and recovery
  - Health check endpoints for monitoring

- **Performance Optimizations**
  - Connection pooling for MongoDB
  - Kafka producer optimizations with batching and compression
  - Efficient document processing using Java Streams
  - Memory management for large datasets
  - Thread-safe operations

- **Configuration & Deployment**
  - Flexible configuration via environment variables or properties files
  - Docker containerization support
  - Kubernetes deployment manifests
  - AWS EKS compatibility
  - Environment-specific configurations

- **Monitoring & Observability**
  - Comprehensive metrics collection
  - Structured logging with SLF4J and Logback
  - Health endpoints (`/health`, `/ready`) for Kubernetes probes
  - Circuit breaker status monitoring
  - Processing statistics and error tracking

- **Testing**
  - 67 comprehensive unit tests
  - Integration test framework
  - Test coverage reporting with JaCoCo
  - Mock-based testing for external dependencies

- **Documentation**
  - Comprehensive README with usage examples
  - Architecture documentation
  - Design patterns documentation
  - Performance optimization guides
  - Deployment guides
  - Testing strategy documentation

### Security
- **Fixed Critical Vulnerabilities**
  - CWE-94: Code injection prevention in operation type handling
  - Input sanitization for MongoDB operation types
  - Secure handling of external data inputs

### Performance
- **Kafka Producer Optimizations**
  - Implemented proper batching mechanism
  - Reduced network overhead through batch processing
  - Optimized flush behavior for better throughput

### Technical Details
- **Dependencies**
  - Java 11+ compatibility
  - MongoDB Java Driver 4.11.1
  - Apache Kafka Client 3.6.0
  - AWS SDK for Java 2.21.29
  - JUnit 5 for testing
  - Mockito for mocking
  - SLF4J with Logback for logging

- **Build & Packaging**
  - Maven-based build system
  - Fat JAR packaging with all dependencies
  - Docker multi-stage build support
  - JaCoCo code coverage integration

### Known Issues
- Test compatibility issues with new implementation structure (being addressed)
- Need to ensure backward compatibility with existing databases
- Large documents may require Kafka `max.request.size` adjustment

## [0.0.1] - 2024-11-01

### Added
- Initial project structure
- Basic MongoDB to Kafka streaming functionality
- Core application framework

---

## Release Notes

### Version 0.1.0 Highlights

This release represents a major milestone in the project's development, transforming it from a basic streaming application into an enterprise-grade, production-ready solution. Key improvements include:

🚀 **Production Ready**: Complete with health checks, metrics, and monitoring
🛡️ **Security Hardened**: Fixed critical vulnerabilities and implemented secure coding practices  
⚡ **Performance Optimized**: Significant throughput improvements through batching and connection pooling
🏗️ **Enterprise Architecture**: Modular design with proper separation of concerns
🧪 **Thoroughly Tested**: 67 tests ensuring reliability and maintainability
📚 **Well Documented**: Comprehensive documentation for users and contributors

### Migration Guide

If upgrading from a previous version:

1. **Configuration Changes**: Review new configuration options in the README
2. **Dependencies**: Ensure Java 11+ and updated MongoDB/Kafka versions
3. **Testing**: Run the full test suite to verify compatibility
4. **Monitoring**: Set up health check endpoints for production deployments

### Contributors

- [@ghoshp83](https://github.com/ghoshp83) - Project maintainer and primary contributor

---

For more details about any release, please check the [GitHub Releases](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/releases) page.