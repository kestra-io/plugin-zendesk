package io.kestra.plugin.zendesk.tickets;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.zendesk.ZendeskConnection;
import io.kestra.plugin.zendesk.models.Ticket;
import io.kestra.plugin.zendesk.models.TicketRequest;
import io.kestra.plugin.zendesk.models.TicketResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Opens new ticket at Zendesk."
)
@Plugin(
    examples = {
        @Example(
            title = "Create Zendesk ticket using username and token.",
            full = true,
            code = """
                   id: zendesk_flow
                   namespace: company.team
                   
                   tasks:
                     - id: create_ticket
                       type: io.kestra.plugin.zendesk.tickets.Create
                       domain: mycompany.zendesk.com
                       username: my_email@example.com
                       token: zendesk_api_token
                       subject: "Increased 5xx in Demo Service"
                       description: |
                         "The number of 5xx has increased beyond the threshold for Demo service."
                       priority: NORMAL
                       ticketType: INCIDENT
                       assigneeId: 1
                       tags:
                         - bug
                         - workflow
                   """
        ),
        @Example(
            title = "Create Zendesk ticket using OAuth token.",
            full = true,
            code = """
                   id: zendesk_flow
                   namespace: company.team
                   
                   tasks:
                     - id: create_ticket
                       type: io.kestra.plugin.zendesk.tickets.Create
                       domain: mycompany.zendesk.com
                       oauthToken: zendesk_oauth_token
                       subject: "Increased 5xx in Demo Service"
                       description: |
                         "The number of 5xx has increased beyond the threshold for Demo service."
                       priority: NORMAL
                       ticketType: INCIDENT
                       assigneeId: 1
                       tags:
                         - bug
                         - workflow
                   """
        ),
        @Example(
            title = "Create a ticket when a Kestra workflow in any namespace with `company` as prefix fails.",
            full = true,
            code = """
                   id: create_ticket_on_failure
                   namespace: company.team
                   
                   tasks:
                     - id: create_ticket
                       type: io.kestra.plugin.zendesk.tickets.Create
                       domain: mycompany.zendesk.com
                       oauthToken: zendesk_oauth_token
                       subject: Workflow failed
                       description: |
                         "{{ execution.id }} has failed on {{ taskrun.startDate }}.
                         See the link below for more details."
                       priority: NORMAL
                       ticketType: INCIDENT
                       assigneeId: 1
                       tags:
                         - bug
                         - workflow
                   triggers:
                     - id: on_failure
                       type: io.kestra.plugin.core.trigger.Flow
                       conditions:
                         - type: io.kestra.plugin.core.condition.ExecutionStatusCondition
                           in:
                             - FAILED
                             - WARNING
                         - type: io.kestra.plugin.core.condition.ExecutionNamespaceCondition
                           namespace: company
                           comparison: PREFIX
                   """
        )
    }
)
public class Create extends ZendeskConnection implements RunnableTask<Create.Output> {

    @Getter
    @RequiredArgsConstructor
    public enum Priority {
        URGENT(),
        HIGH(),
        NORMAL(),
        LOW();

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        PROBLEM(),
        INCIDENT(),
        QUESTION(),
        TASK();

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    @Schema(
        title = "Ticket subject"
    )
    @PluginProperty(dynamic = true)
    private String subject;

    @Schema(
        title = "Ticket description"
    )
    @PluginProperty(dynamic = true)
    private String description;

    @Schema(
        title = "Priority",
        description = """
                      Available values:
                       - URGENT
                       - HIGH
                       - NORMAL
                       - LOW
                      """
    )
    @PluginProperty
    private Priority priority;

    @Schema(
        title = "Ticket type",
        description = """
                      Available values:
                       - PROBLEM
                       - INCIDENT
                       - QUESTION
                       - TASK
                      """
    )
    @PluginProperty
    private Type ticketType;

    @Schema(
        title = "Id of assignee"
    )
    @PluginProperty
    private Long assigneeId;

    @Schema(
        title = "List of tags for ticket"
    )
    @PluginProperty(dynamic = true)
    private List<String> tags;

    @Override
    public Create.Output run(RunContext runContext) throws Exception {
        Ticket.TicketBuilder request = Ticket.builder()
            .subject(runContext.render(this.subject))
            .description(runContext.render(this.description))
            .priority(this.priority.toString())
            .type(this.ticketType.toString())
            .tags(runContext.render(this.tags));

        Optional
            .ofNullable(assigneeId)
            .ifPresent(request::id);

        String requestBody = mapper.writeValueAsString(new TicketRequest(request.build()));

        Ticket response = makeCall(runContext, requestBody, TicketResponse.class).getTicket();

        return Output.builder()
            .id(response.getId())
            .url(response.getUrl())
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {

        @Schema(
            title = "Ticket URL"
        )
        private final String url;

        @Schema(
            title = "Ticket id"
        )
        private final Long id;

    }

}
