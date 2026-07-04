# Project Structure

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5, Java 17, Maven multi-module |
| Frontend | Vue.js 3, Vite, TypeScript, Tailwind CSS |
| Testing | JUnit 5 (Java), Vitest (UI), Playwright |
| Build | Maven 3.9+, Node.js 22.18.0, npm |
| Code Quality | Spring Java Format, Checkstyle, ESLint, Prettier |

## Maven Modules (19 total)

### Core

| Module | Path | Purpose |
|---|---|---|
| Root reactor | `/` | Main `pom.xml` |
| Server Backend | `spring-boot-admin-server/` | Core server functionality |
| UI Frontend | `spring-boot-admin-server-ui/` | Vue.js 3 web interface (Vite) |
| Client | `spring-boot-admin-client/` | Client library for connecting apps |
| Documentation | `spring-boot-admin-docs/` | Asciidoc documentation |

### Starters

| Module | Purpose |
|---|---|
| `spring-boot-admin-starter-server/` | Auto-configuration for servers |
| `spring-boot-admin-starter-client/` | Auto-configuration for clients |

### Infrastructure

| Module | Purpose |
|---|---|
| `spring-boot-admin-dependencies/` | Dependency management BOM |
| `spring-boot-admin-build/` | Build configuration and tooling |
| `spring-boot-admin-server-cloud/` | Spring Cloud integrations |

### Sample Applications (`spring-boot-admin-samples/`)

| Sample | Notes |
|---|---|
| `servlet` | Standard servlet-based — **recommended for testing** |
| `reactive` | WebFlux reactive |
| `eureka` | Eureka discovery integration |
| `consul` | Consul discovery integration |
| `hazelcast` | Hazelcast session management |
| `war` | WAR deployment |
| `zookeeper` | Zookeeper discovery integration |

## Repository Layout

```
spring-boot-admin/
├── .github/                         # GitHub configuration & instructions
├── spring-boot-admin-build/
├── spring-boot-admin-client/
├── spring-boot-admin-dependencies/
├── spring-boot-admin-docs/
├── spring-boot-admin-samples/
│   ├── spring-boot-admin-sample-servlet/   # Main sample (recommended)
│   ├── spring-boot-admin-sample-reactive/
│   └── [other samples]/
├── spring-boot-admin-server/
├── spring-boot-admin-server-cloud/
├── spring-boot-admin-server-ui/
│   ├── src/main/frontend/          # Vue.js source code
│   ├── package.json
│   └── .nvmrc                      # Node.js version specification
├── spring-boot-admin-starter-*/
├── mvnw
└── pom.xml
```
