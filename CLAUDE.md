# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew.bat build

# Run
./gradlew.bat bootRun

# Test (all)
./gradlew.bat test

# Single test class
./gradlew.bat test --tests "com.example.demo.DemoApplicationTests"

# Clean
./gradlew.bat clean
```

## Architecture

Spring Boot 3.x REST API (Java 21) with an H2 in-memory database. Follows a standard layered structure:

- **Controller** (`PostController`) — REST endpoints at `/posts`, maps HTTP verbs to service calls
- **Service** (`PostService`) — business logic, converts between `Post` entity and `PostDto`
- **Repository** (`PostRepository`) — Spring Data JPA interface, no implementation needed
- **Entity** (`Post`) — JPA-mapped table with `id`, `title`, `content`; schema is `create-drop` so it resets on each restart
- **DTO** (`PostDto`) — inbound/outbound transfer object (title + content only; id is excluded from input)

The H2 console is accessible at `/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`) while the app is running, useful for inspecting data. SQL is logged to stdout via `spring.jpa.show-sql=true`.

Tests use `@SpringBootTest` for full integration context loads.