# Kestra Zendesk Plugin

## What

- Provides plugin components under `io.kestra.plugin.zendesk`.
- Includes classes such as `ZendeskConnection`, `Ticket`, `TicketRequest`, `TicketResponse`.

## Why

- What user problem does this solve? Teams need to interact with the Zendesk API for ticketing from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Zendesk steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Zendesk.

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
