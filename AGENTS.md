# Kestra Zendesk Plugin

## What

- Provides plugin components under `io.kestra.plugin.zendesk`.
- Includes classes such as `ZendeskConnection`, `Ticket`, `TicketRequest`, `TicketResponse`.

## Why

- This plugin integrates Kestra with Zendesk.
- It provides tasks that interact with the Zendesk API for ticketing.

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

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
