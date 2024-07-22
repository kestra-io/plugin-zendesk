package io.kestra.plugin.zendesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class ZendeskConnection extends Task {

    protected final static ObjectMapper mapper = JacksonMapper.ofJson();

    public static final String ZENDESK_URL_FORMAT = "https://%s/api/v2/tickets.json";

    @Schema(
        title = "Zendesk domain url"
    )
    @NotNull
    @PluginProperty(dynamic = true)
    private String domain;

    @Schema(
        title = "Zendesk username"
    )
    @PluginProperty(dynamic = true)
    private String username;

    @Schema(
        title = "Zendesk api token"
    )
    @PluginProperty(dynamic = true)
    private String token;

    @Schema(
        title = "Zendesk oauth token, if api token and username is not provided"
    )
    @PluginProperty(dynamic = true)
    private String oauthToken;

    public <T> T makeCall(RunContext runContext, String body, Class<T> clazz) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = ZENDESK_URL_FORMAT.formatted(runContext.render(this.domain));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", getAuthorizationHeader(runContext))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new IOException("Failed to create ticket. Response: " + response.body());
            }

            return mapper.readValue(response.body(), clazz);
        }
    }

    private String getAuthorizationHeader(RunContext runContext) throws IllegalVariableEvaluationException {
        if (this.token != null && !this.token.isEmpty()) {
            return "Basic " + Base64
                .getEncoder()
                .encodeToString(
                    "%s/token:%s"
                        .formatted(runContext.render(this.username), runContext.render(this.token))
                        .getBytes()
                );
        } else if (this.oauthToken != null && !this.oauthToken.isEmpty()) {
            return "Bearer " + this.oauthToken;
        }

        throw new IllegalArgumentException("Authentication details are missing");
    }

}
