package io.kestra.plugin.zendesk.tickets;

import com.google.common.base.Strings;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@KestraTest
class CreateTest {
    @Value("${zendesk.url}")
    private String zendeskUrl;

    @Value("${zendesk.email}")
    private String zendeskEmail;

    @Value("${zendesk.token}")
    private String zendeskToken;

    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run() throws Exception {
        RunContext runContext = runContextFactory.of();

        Create task = Create.builder()
            .domain(Property.ofValue(zendeskUrl))
            .username(Property.ofValue(zendeskEmail))
            .token(Property.ofValue(zendeskToken))
            .subject(Property.ofValue("Test Ticket"))
            .description("This is a test ticket from Kestra Unit Tests")
            .priority(Property.ofValue(Create.Priority.NORMAL))
            .ticketType(Property.ofValue(Create.Type.TASK))
            .tags(Property.ofValue(List.of("kestra", "bug", "workflow")))
            .build();

        Create.Output runOutput = task.run(runContext);

        assertThat(runOutput, is(notNullValue()));
        assertThat(runOutput.getId(), is(notNullValue()));
    }
}
