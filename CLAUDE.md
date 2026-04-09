# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Quarkus Chappie is a **dev-mode only** Quarkus extension that adds AI-powered assistance to the Quarkus developer experience. It provides exception help, code generation, documentation search (RAG), and chat — all accessible through the Quarkus Dev UI. Nothing is added to production builds.

The extension spawns a separate `chappie-server` process (downloaded as a JAR from Maven) that handles LLM interactions. The server supports OpenAI, Anthropic, Gemini, WatsonX, and Ollama providers.

## Build Commands

```bash
# Full build
mvn clean install

# Fast build (skip tests and docs)
mvn clean install -DskipTests -Ddocs.skip

# Build a single module
mvn clean install -pl runtime-dev

# Run integration tests (requires Docker)
mvn clean install -DskipITs=false

# Run the sample app (then configure provider in Dev UI)
cd sample && mvn quarkus:dev
```

There are no linters or formatters configured.

**Note on profiles:** Both `docs` and `it` profiles auto-activate when `performRelease != true`. Integration tests have `skipITs=true` by default, so pass `-DskipITs=false` to run them.

## Module Structure

| Module | Artifact | Purpose |
|--------|----------|---------|
| `runtime/` | `quarkus-chappie` | Extension manifest only — no Java code. Declares capability `io.quarkus.assistant`. |
| `runtime-dev/` | `quarkus-chappie-dev` | Dev-mode runtime: server process management, HTTP client to chappie-server, Dev UI JSON-RPC backend, JSON schema generation. Conditionally loaded only in dev mode. |
| `deployment/` | `quarkus-chappie-deployment` | Build-time processors: DevServices (pgvector container), Dev UI pages, exception handling, workspace actions (JavaDoc/tests/explain/TODO). |
| `sample/` | — | Test application with simulated exceptions at `/nullpointer`, `/arithmetic`, etc. |
| `integration-tests/` | — | Integration tests (skipped by default, enable with `-DskipITs=false`). |
| `docs/` | — | Extension documentation. Skip with `-Ddocs.skip`. |

## Architecture

### Quarkus Extension Pattern

The extension uses standard Quarkus extension conventions:

- **`@BuildSteps(onlyIf = IsLocalDevelopment.class)`** — All processors are guarded to run only in local dev mode
- **`ChappieRecorder`** — `@Record(RUNTIME_INIT)` recorder that creates `ChappieServerManager` and maps RAG datasource config
- **Build items** for inter-processor communication: `LastExceptionBuildItem`, `LastSolutionBuildItem`, `BroadcastsBuildItem`, `ExtensionVersionBuildItem`

The `Assistant` SPI interface comes from Quarkus core (`quarkus-assistant-deployment-spi` / `quarkus-assistant-runtime-dev`), not from this extension. This extension provides the `ChappieAssistant` implementation.

### Configuration

The config prefix is `quarkus.assistant` (not `quarkus.chappie`). Build-time config at `quarkus.assistant.augmenting.enabled` (default `true`) in `ChappieConfig` — this controls whether the pgvector DevService container starts.

Runtime provider config is managed by `ChappieServerManager` and persisted to `~/.quarkus/chappie/chappie-assistant.properties`, not `application.properties`.

### Key Classes (runtime-dev)

- **`ChappieServerManager`** — Core lifecycle manager. Downloads/installs `chappie-server.jar` from Maven, spawns it as a JVM subprocess, manages port allocation (starting at 4315), handles configuration persistence, streams logs via `SubmissionPublisher`, and subscribes to `McpEvent` stream to forward MCP server URLs to chappie-server.
- **`ChappieAssistant`** — HTTP client implementing the `Assistant` interface. Communicates with chappie-server via REST at `/api/assist`. Auto-detects caller extensions via stack walking for RAG context.
- **`ChappieJsonRpcService`** — Dev UI backend. JSON-RPC methods for configuration, chat, and chat history management.
- **`JsonObjectCreator`** — Generates JSON schemas for expected response types using `jsonschema-generator`. Builds request payloads with system/user messages, variables, and response schemas. Parses responses via `ChappieEnvelope<T>`.
- **`ChappieEnvelope<T>`** — Generic response wrapper record: `{ niceName, answer }`.
- **`ChappieRecorder`** — Maps RAG datasource config from DevServices keys (`chappie.rag.*`) to chappie-server keys (`quarkus.datasource.chappie.*`).

### Key Classes (deployment)

- **`ChappieProcessor`** — Main processor. Creates beans, discovers extension version from `quarkus-extension.yaml`, sets up console commands, starts pgvector DevService container (`ghcr.io/quarkusio/chappie-ingestion-quarkus:<version>`).
- **`ChappieDevUIProcessor`** — Registers Dev UI pages (Configure, Chat), JSON-RPC service, and MCP search action (`searchDocs`).
- **`ExceptionDevUIProcessor`** — Exception help feature: reactive exception broadcasting via `BroadcastProcessor`, "Get help with this" error page link, AI-powered fix suggestions with diff, apply-fix capability.
- **`BuiltInActionsProcessor`** — Workspace actions: Add JavaDoc, Generate Test, Explain, Complete TODO. Each uses prompt templates from corresponding `*Prompts` classes.

### RAG Config Flow

DevServices → Recorder → ChappieServerManager → chappie-server subprocess:
1. `startPgvectorDevService()` produces config keys like `chappie.rag.jdbc.url`
2. `ChappieRecorder` maps these to `quarkus.datasource.chappie.*` keys
3. `ChappieServerManager.getChappieServerArguments()` passes them as `-D` JVM args to the subprocess

### DevServices

The `startPgvectorDevService()` BuildStep in `ChappieProcessor` launches a PostgreSQL container with pre-baked RAG embeddings using TestContainers. The container image tag matches the Quarkus version, falling back to "latest" for SNAPSHOT versions.

### Dev UI Integration

Web components (in `runtime-dev/src/main/resources/dev-ui/`):
- `qwc-chappie-configure.js` — AI provider settings
- `qwc-chappie-chat.js` — Chat interface
- `qwc-chappie-exception.js` — Exception help page
- `qwc-chappie-init.js` — Headless initialization

## Cross-Project Development

To test changes to the chappie-server with this extension:

1. Clone and build chappie-server: `mvn clean install -Dquarkus.profile=chappie`
2. In `runtime-dev/pom.xml`, change `<chappie-server.version>` to `999-SNAPSHOT`
3. Rebuild this extension after each server change

## Key Dependency

The `runtime-dev/pom.xml` uses `maven-dependency-plugin` to copy the `chappie-server.jar` into `target/classes/bin/` at build time. The version is controlled by the `<chappie-server.version>` property (currently `1.1.9`).
