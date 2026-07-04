# Environment Setup

## Prerequisites

- **Java 17** (OpenJDK Temurin 17.0.16+ recommended) — project requires exactly Java 17
- **Node.js 22.18.0** exactly — specified in `.nvmrc` and `package.json`

## Node.js Setup

```bash
curl -fsSL https://nodejs.org/dist/v22.18.0/node-v22.18.0-linux-x64.tar.xz -o /tmp/node.tar.xz
cd /tmp && tar -xf node.tar.xz
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH"
```

Verify versions:

```bash
java -version   # must show 17.x
node --version  # must show v22.18.0
```

## PATH Setup (required before any build/test command)

Always prefix build commands with:

```bash
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH"
```
