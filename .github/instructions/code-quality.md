# Code Quality and Formatting

> **Always** run formatting and linting before committing.

## Java

```bash
# Auto-fix formatting
./mvnw spring-javaformat:apply

# Validate formatting
./mvnw spring-javaformat:validate

# Check Checkstyle compliance
./mvnw checkstyle:check
```

## UI (Vue.js / TypeScript)

```bash
cd spring-boot-admin-server-ui

# Lint
npm run lint

# Auto-fix formatting
npm run format:fix
```

## Pre-commit Checklist

1. `./mvnw spring-javaformat:apply`
2. `cd spring-boot-admin-server-ui && npm run format:fix`
3. `./mvnw checkstyle:check` — must pass with no violations

## Tooling

| Layer | Tool |
|---|---|
| Java | Spring Java Format, Checkstyle |
| UI | ESLint, Prettier |
