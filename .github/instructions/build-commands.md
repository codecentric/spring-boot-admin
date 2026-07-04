# Build Commands

> **NEVER CANCEL builds** — they take time but will complete. Set timeouts accordingly.

## Timing Reference

| Command | Duration |
|---|---|
| Clean compile | ~10.5 min |
| Package (skip tests) | ~4 min |
| Install (skip tests) | ~1.5 min |
| Full test suite | 20–40 min |
| UI tests only | ~44 sec |
| UI build only | ~16 sec |
| Application startup | ~3 sec |

## Maven Build Commands

All commands require the Node.js PATH export — see [environment-setup.md](environment-setup.md).

```bash
# Clean compile (verify everything builds)
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && ./mvnw clean compile -B --no-transfer-progress -DskipTests

# Full package
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && ./mvnw package -B --no-transfer-progress -DskipTests

# Install to local repository (required before running samples)
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && ./mvnw install -B --no-transfer-progress -DskipTests
```

## Test Commands

> Set **60+ minute timeouts** for test runs — never cancel them.

```bash
# Full test suite
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && ./mvnw test -B --no-transfer-progress

# UI tests only (~44 seconds)
cd spring-boot-admin-server-ui && export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && npm run test

# UI build only (~16 seconds)
cd spring-boot-admin-server-ui && export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && npm run build
```

## Development Workflow

1. **Setup**: `export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH"`
2. **Initial Build**: `./mvnw clean compile` (~10.5 min — be patient)
3. **Install**: `./mvnw install -DskipTests` (~1.5 min, for sample app dependencies)
4. **Make Changes**: Edit code in appropriate modules
5. **Format Code**: Run formatting before committing (see [code-quality.md](code-quality.md))
6. **Verify Build**: Run build commands to confirm changes compile
7. **Manual Validation**: Start sample app and test UI (see [running-samples.md](running-samples.md))
