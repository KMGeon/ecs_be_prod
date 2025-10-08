# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Spring Boot 3.5.6 REST API application built with Gradle (Kotlin DSL) and Java 17.

## Build and Run Commands

### Build
```bash
./gradlew build
```

### Run Application
```bash
./gradlew bootRun
```

### Run Tests
```bash
./gradlew test
```

### Run Single Test Class
```bash
./gradlew test --tests "TestClassName"
```

### Clean Build
```bash
./gradlew clean build
```

### Docker Compose
```bash
docker-compose up -d
```

## Architecture

### Package Structure
- `me.geon.ecs_be_prod` - Base package
  - `Controller.java` - REST endpoints
  - `EcsBeProdApplication.java` - Spring Boot main application class

### Configuration
- `src/main/resources/application.yml` - Spring Boot configuration
- `build.gradle.kts` - Gradle build configuration with Kotlin DSL
- `docker-compose.yml` - Docker services configuration

### Key Technologies
- Spring Boot 3.5.6
- Spring Web for REST APIs
- Gradle with Kotlin DSL
- Java 17 LTS
- JUnit for testing


[//]: # (docker run -it --env-file .env  ecs-be-prod)
