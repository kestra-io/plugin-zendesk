# Kestra Zendesk Plugin

## What

Plugin Zendesk for Kestra Exposes 1 plugin components (tasks, triggers, and/or conditions).

## Why

Enables Kestra workflows to interact with Zendesk, allowing orchestration of Zendesk-based operations as part of data pipelines and automation workflows.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `zendesk`

Infrastructure dependencies (Docker Compose services):

- `zendesk`

### Key Plugin Classes

- `io.kestra.plugin.zendesk.tickets.Create`

### Project Structure

```
plugin-zendesk/
├── src/main/java/io/kestra/plugin/zendesk/tickets/
├── src/test/java/io/kestra/plugin/zendesk/tickets/
├── build.gradle
└── README.md
```

### Important Commands

```bash
# Build the plugin
./gradlew shadowJar

# Run tests
./gradlew test

# Build without tests
./gradlew shadowJar -x test
```

### Configuration

All tasks and triggers accept standard Kestra plugin properties. Credentials should use
`{{ secret('SECRET_NAME') }}` — never hardcode real values.

## Agents

**IMPORTANT:** This is a Kestra plugin repository (prefixed by `plugin-`, `storage-`, or `secret-`). You **MUST** delegate all coding tasks to the `kestra-plugin-developer` agent. Do NOT implement code changes directly — always use this agent.
