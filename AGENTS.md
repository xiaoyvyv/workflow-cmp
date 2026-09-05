# AGENTS.md - Developer & AI Agent Guidelines

Welcome to the **workflow-cmp** repository. This document provides core architectural principles, module topologies, invariant rules, and developer commands to guide AI coding
agents and human developers when interacting with or modifying this codebase.

---

## 1. Project Overview & Architecture

**workflow-cmp** is a declarative DAG automation and workflow orchestration engine built for **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)**. It supports
Android, iOS, and Desktop (JVM) with consistent behavior across platforms.

### 1.1 Module Topology

```text
workflow-cmp/
  ├── :workflow-core                 # [Core Engine] Data models, DAG topological sorting, coroutines scheduler, expression evaluator
  │
  ├── Node Ecosystem (workflow-node-*)
  │   ├── :workflow-node-base        # [Node Base] Contracts, ActionNodeDefinition, Manifest models, registry
  │   ├── :workflow-node-control     # [Control/Branch] Flow controls (flow.*) & logic (control.*)
  │   ├── :workflow-node-data        # [Data/Transform] Context, variables, Object/Array/Text/Math/Date/URL/JSON/XML/CSV
  │   ├── :workflow-node-codec       # [Codec] Base64, Hex, URL encoding/decoding, HTML escape
  │   ├── :workflow-node-html        # [DOM Extraction] HTML parsing & CSS selector extraction via Ksoup
  │   ├── :workflow-node-crypto      # [Cryptography] MD5, SHA, HMAC, AES encryption/decryption
  │   ├── :workflow-node-io          # [I/O] HTTP requests/downloads, Preferences storage, sandbox file system
  │   ├── :workflow-node-bilibili    # [Domain Extension] Bilibili Wbi signature & API adapter
  │   ├── :workflow-node-all         # [Assembly] Built-in node aggregator & WorkflowRuntimeFactory
  │   └── :workflow-node-testkit     # [Testkit] Automated node specification and execution testing tools
  │
  ├── Editor & MCP Ecosystem (workflow-editor-*)
  │   ├── :workflow-editor-bridge    # [KMP Bridge Server] Embedded Ktor CIO HTTP / WebSocket server, run controller
  │   ├── :workflow-editor-mcp       # [MCP AI Protocol] Model Context Protocol stdio server for Claude/Cursor AI integration
  │   └── workflow-editor-web/       # [Web DAG Canvas] Vanilla ES6 frontend, drag-and-drop DAG canvas, GitHub Pages ready
  │
  ├── Platform Infrastructure (workflow-platform-*)
  │   ├── :workflow-platform         # [Base Platform] Volume/brightness, full screen controller
  │   ├── :workflow-platform-room    # [Room 3 Storage] Multiplatform CookieJar & local persistence
  │   └── :workflow-platform-ui      # [Compose Platform] SystemService, BackHandler
  │
  ├── UI & Interaction (workflow-ui-*)
  │   ├── :workflow-ui-core          # [UI Core] SideEffect host state, standard dialogs
  │   ├── :workflow-ui-image         # [Image Gallery] Multiplatform full screen image viewer with gestures & strip mode
  │   ├── :workflow-ui-video         # [Video Player] Multiplatform video preview component
  │   ├── :workflow-ui-webview       # [Web Container] Embedded Webview & Cookie synchronization dialog
  │   └── :workflow-ui-all           # [UI Assembly] Default SideEffect host (WorkflowDefaultSideEffectHost)
  │
  └── Demo & Applications
      ├── :shared                    # Multiplatform shared demo UI & ViewModels
      ├── :androidApp                # Android application entry
      └── :desktopApp                # Desktop (JVM) application entry
```

---

## 2. Invariants & Development Rules

### 2.1 Single Source of Truth for Metadata

- **Backend-Driven Specs**: The Kotlin backend (`ActionNodeRegistry` -> `EditorWorkflowManifest`) is the **sole source of truth** for all node definitions, input/output port types,
  configuration forms, and default properties.
- **No Hardcoding in Frontend**: The Web editor (`workflow-editor-web`) must NEVER hardcode node categories, port lists, or side-effect descriptions. All display data must be
  parsed dynamically from the backend Manifest and execution events.

### 2.2 DAG Scheduling & Validation

- **Single Entry Rule**: Every valid executable workflow must contain **exactly one** entry node (`entry` type = 1). The Web editor must validate this and prevent creating multiple
  entry nodes.
- **Acyclic Enforcement**: The graph must be an acyclic directed graph (DAG) during topology calculation, with loops handled cleanly via explicit loop nodes (`loop.*`).
- **Trace & Error Consistency**: Runtime errors and execution failure stack traces must consistently follow the formatting defined in `buildFormattedTraceLog` (Node ID, Node Type,
  Step, Output & Error Reason).

### 2.3 SideEffect Decoupling (Human-in-the-Loop)

- Engine nodes must never directly reference UI elements or framework context.
- Interactive actions (e.g. alerts, input dialogs, image preview, webview cookie extraction) must emit `ActionSideEffect` events. The host application suspends the workflow until
  the user acts and submits the result.

### 2.4 Optimistic Locking & Concurrency

- `EditorWorkflowRepository` operations use an explicit `revision` counter.
- Updates must provide the expected revision number. If a conflict occurs, `EditorSaveResult.Conflict` is returned to prevent accidental overwrites.

---

## 3. Web Editor & CI/CD

- **Web Frontend**: Located at `workflow-editor-web/`. Built with pure HTML/CSS/ES6 modules (no heavy build framework needed).
- **GitHub Pages CI**: Configured in `.github/workflows/deploy-editor-web.yml`. Pushes to `main` automatically publish the Web editor to GitHub Pages (
  `https://xiaoyvyv.github.io/workflow-cmp/`).
- **Local Embedded Server**: The Kotlin bridge server (`:workflow-editor-bridge`) embeds the web assets and serves them on `http://127.0.0.1:8080/`.

---

## 4. Common Developer Commands

### 4.1 Running Tests

```bash
# Run JVM tests for core engine, editor bridge and MCP
./gradlew :workflow-core:jvmTest :workflow-editor-bridge:jvmTest :workflow-editor-mcp:jvmTest

# Run all JVM tests across all node modules
./gradlew test
```

### 4.2 Running Demo Applications

```bash
# Run Desktop (JVM) Compose Multiplatform application
./gradlew :desktopApp:run

# Build Android APK
./gradlew :androidApp:assembleDebug
```

---

## 5. Coding & Documentation Guidelines

- **File Encoding & Style**: Standard Kotlin formatting, Kotlin 2.4+, Compose Multiplatform 1.12+.
- **Documentation**: Keep `README.md` and `docs/` in sync whenever node specifications, REST APIs (`/api/v1/*`), or bridge protocols are modified.
