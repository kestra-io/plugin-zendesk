package io.kestra.plugin.zendesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
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

    public static final String ZENDESK_URL_FORMAT = "%s/api/v2/tickets.json";

    @Schema(
        title = "Zendesk domain url"
    )
    @NotNull
    private Property<String> domain;

    @Schema(
        title = "Zendesk username"
    )
    private Property<String> username;

    @Schema(
        title = "Zendesk api token"
    )
    private Property<String> token;

    @Schema(
        title = "Zendesk oauth token, if api token and username is not provided"
    )
    private Property<String> oauthToken;

    public <T> T makeCall(RunContext runContext, String body, Class<T> clazz) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String rawUrl = runContext.render(this.domain).as(String.class).orElseThrow();
            String baseUrl = normaliseBase(rawUrl);
            String url  = ZENDESK_URL_FORMAT.formatted(baseUrl);

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
        var renderedToken = runContext.render(this.token).as(String.class);
        var renderedOauthToken = runContext.render(this.oauthToken).as(String.class);
        if (renderedToken.isPresent() && !renderedToken.get().isEmpty()) {
            return "Basic " + Base64
                .getEncoder()
                .encodeToString(
                    "%s/token:%s"
                        .formatted(runContext.render(this.username).as(String.class).orElse(null), renderedToken.get())
                        .getBytes()
                );
        } else if (renderedOauthToken.isPresent() && !renderedOauthToken.get().isEmpty()) {
            return "Bearer " + renderedOauthToken.get();
        }

        throw new IllegalArgumentException("Authentication details are missing");
    }

    protected static String normaliseBase(String base) {
        if (base.startsWith("http://") || base.startsWith("https://")) {
            return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        }
        return "https://" + base;
    }

}
