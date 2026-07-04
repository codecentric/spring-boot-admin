# Troubleshooting

## Build Issues

| Symptom | Cause | Fix |
|---|---|---|
| Node.js version mismatch | Wrong Node version in PATH | Use exact PATH override with v22.18.0 |
| Missing dependencies | Modules not in local Maven repo | Run `./mvnw install` |
| Checkstyle violations | Formatting issues | Run `./mvnw spring-javaformat:apply` |
| UI build failures | Wrong Node or missing packages | Verify Node.js version; run `npm install` in `spring-boot-admin-server-ui/` |
| "Command timed out" | Build takes 10+ minutes legitimately | Increase timeout — never cancel |

## Runtime Issues

| Symptom | Cause | Fix |
|---|---|---|
| Sample app can't resolve dependencies | Local Maven repo not populated | Run `./mvnw install` first |
| Config server connection refused | Config server not running | Normal warning — config server is optional in dev mode |
| Authentication required | Security enabled | Use `insecure` profile: `-Dspring-boot.run.profiles=dev,insecure` |

## Key Reminders

- **NEVER CANCEL** builds or tests — set 20+ minute timeouts for builds, 60+ for tests
- Always set `export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH"` before any Maven or npm command
- Run `./mvnw install` before running sample applications
