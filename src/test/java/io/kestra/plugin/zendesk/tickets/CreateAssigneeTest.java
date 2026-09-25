package io.kestra.plugin.zendesk.tickets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// Regression test for the bug where `assigneeId` was sent as the ticket `id` instead of
// `assignee_id`. Runs against a local HTTP server so it needs no Zendesk credentials.
@KestraTest
class CreateAssigneeTest {

    @Inject
    private RunContextFactory runContextFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsAssigneeIdAsAssigneeIdNotTicketId() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        AtomicReference<String> capturedAuthorization = new AtomicReference<>();

        server = startServer(capturedBody, capturedAuthorization);

        RunContext runContext = runContextFactory.of();

        Create task = Create.builder()
            .domain(Property.ofValue("http://localhost:" + server.getAddress().getPort()))
            .username(Property.ofValue("my_email@example.com"))
            .token(Property.ofValue("api-token"))
            .subject(Property.ofValue("Test Ticket"))
            .description("This is a test ticket")
            .assigneeId(Property.ofValue(1234L))
            .build();

        Create.Output runOutput = task.run(runContext);

        assertThat(runOutput.getId(), is(42L));
        assertThat(capturedAuthorization.get(), notNullValue());

        JsonNode ticket = mapper.readTree(capturedBody.get()).get("ticket");
        assertThat(ticket.get("assignee_id").asLong(), is(1234L));
        assertThat(ticket.has("id"), is(false));
    }

    @Test
    void omitsAssigneeIdWhenNotSet() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        AtomicReference<String> capturedAuthorization = new AtomicReference<>();

        server = startServer(capturedBody, capturedAuthorization);

        RunContext runContext = runContextFactory.of();

        Create task = Create.builder()
            .domain(Property.ofValue("http://localhost:" + server.getAddress().getPort()))
            .username(Property.ofValue("my_email@example.com"))
            .token(Property.ofValue("api-token"))
            .subject(Property.ofValue("Test Ticket"))
            .description("This is a test ticket")
            .build();

        task.run(runContext);

        JsonNode ticket = mapper.readTree(capturedBody.get()).get("ticket");
        assertThat(ticket.has("assignee_id"), is(false));
        assertThat(ticket.has("id"), is(false));
    }

    private static HttpServer startServer(AtomicReference<String> capturedBody, AtomicReference<String> capturedAuthorization) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/v2/tickets.json", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            capturedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));

            byte[] response = "{\"ticket\":{\"id\":42,\"url\":\"https://example.zendesk.com/api/v2/tickets/42.json\"}}"
                .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
        return server;
    }
}
