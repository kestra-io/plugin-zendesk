# How to use the Zendesk plugin

Create support tickets in Zendesk from Kestra flows.

## Authentication

Set `domain` to your Zendesk account URL. For API token auth, set `username` (your account email) and `token`. For OAuth, set `oauthToken` instead — it is used as a bearer token and takes precedence over `username`/`token`. Store secrets in [secrets](https://kestra.io/docs/concepts/secret) and apply connection properties globally with [plugin defaults](https://kestra.io/docs/workflow-components/plugin-defaults).

## Tasks

`tickets.Create` creates a support ticket — set `subject` and `description` (required). Optionally set `priority` (`LOW`, `NORMAL`, `HIGH`, or `URGENT`), `ticketType` (`PROBLEM`, `INCIDENT`, `QUESTION`, or `TASK`), `assigneeId`, and `tags`. The output includes the ticket `id` and `url`.
