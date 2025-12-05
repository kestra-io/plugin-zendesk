package io.kestra.plugin.zendesk.tickets;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
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
    title = "Open a new ticket in Zendesk."
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
                       token: "{{ secret('ZENDESK_TOKEN') }}"
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
                       oauthToken: "{{ secret('ZENDESK_OAUTH_TOKEN') }}"
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
                       oauthToken: "{{ secret('ZENDESK_OAUTH_TOKEN') }}"
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
                         - type: io.kestra.plugin.core.condition.ExecutionStatus
                           in:
                             - FAILED
                             - WARNING
                         - type: io.kestra.plugin.core.condition.ExecutionNamespace
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
    private Property<String> subject;

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
    private Property<Priority> priority;

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
    private Property<Type> ticketType;

    @Schema(
        title = "Id of assignee"
    )
    private Property<Long> assigneeId;

    @Schema(
        title = "List of tags for ticket"
    )
    private Property<List<String>> tags;

    @Override
    public Create.Output run(RunContext runContext) throws Exception {
        String rDomain = normaliseBase(runContext.render(this.getDomain()).as(String.class).orElseThrow());

        Ticket.TicketBuilder request = Ticket.builder()
            .subject(runContext.render(this.subject).as(String.class).orElse(null))
            .description(runContext.render(this.description))
            .priority(runContext.render(this.priority).as(Priority.class).map(Priority::toString).orElse(null))
            .type(runContext.render(this.ticketType).as(Create.Type.class).map(Create.Type::toString).orElse(null))
            .tags(runContext.render(this.tags).asList(String.class));

        runContext.render(assigneeId).as(Long.class).ifPresent(request::id);

        String requestBody = mapper.writeValueAsString(new TicketRequest(request.build()));

        Ticket response = makeCall(runContext, requestBody, TicketResponse.class).getTicket();

        return Output.builder()
            .id(response.getId())
            .url(Optional.ofNullable(response.getUrl()).orElse(String.format("%s/api/v2/tickets/%d.json", rDomain, response.getId())))
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
