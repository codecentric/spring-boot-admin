# Spring Boot Admin — Copilot Instructions

Spring Boot Admin is a multi-module Maven project providing an admin interface for Spring Boot applications that expose actuator endpoints. It consists of a Vue.js frontend and Java backend components with 19 Maven modules.

Always reference these instructions first and fall back to search or bash commands only when you encounter unexpected information not covered here.

## Instruction Files

Load only the context relevant to your current task:

| File | When to read |
|---|---|
| [environment-setup.md](instructions/environment-setup.md) | Setting up Java / Node.js, first-time environment |
| [build-commands.md](instructions/build-commands.md) | Compiling, packaging, testing, dev workflow |
| [code-quality.md](instructions/code-quality.md) | Formatting, linting, pre-commit checks |
| [testing.md](instructions/testing.md) | Test structure, naming, Given/When/Then conventions |
| [project-structure.md](instructions/project-structure.md) | Module overview, repo layout, tech stack |
| [running-samples.md](instructions/running-samples.md) | Running sample apps, UI dev mode, validation |
| [troubleshooting.md](instructions/troubleshooting.md) | Common build and runtime issues |

## Critical Rules (always apply)

- **Never cancel builds** — set 20+ minute timeouts for builds, 60+ for tests
- **Always export Node.js PATH** before any build/test: `export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH"`
- **Always run formatting** before committing: `./mvnw spring-javaformat:apply` + `npm run format:fix`
- **Install before running samples**: `./mvnw install -DskipTests`
