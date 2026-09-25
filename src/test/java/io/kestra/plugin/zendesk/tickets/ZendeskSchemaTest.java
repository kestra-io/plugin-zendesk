package io.kestra.plugin.zendesk.tickets;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.kestra.core.docs.JsonSchemaGenerator;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.tasks.Task;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@KestraTest
class ZendeskSchemaTest {

    @Inject
    JsonSchemaGenerator jsonSchemaGenerator;

    @Test
    @SuppressWarnings("unchecked")
    void createPropertiesAreGroupedAndUsernameIsNotSecret() {
        var generate = jsonSchemaGenerator.properties(Task.class, Create.class);
        var properties = (Map<String, Map<String, Object>>) generate.get("properties");

        assertThat(group(properties.get("domain")), is("connection"));
        assertThat(group(properties.get("username")), is("connection"));
        assertThat(group(properties.get("token")), is("connection"));
        assertThat(group(properties.get("oauthToken")), is("connection"));

        assertThat(group(properties.get("subject")), is("main"));
        assertThat(group(properties.get("description")), is("main"));

        assertThat(group(properties.get("priority")), is("advanced"));
        assertThat(group(properties.get("ticketType")), is("advanced"));
        assertThat(group(properties.get("assigneeId")), is("advanced"));
        assertThat(group(properties.get("tags")), is("advanced"));

        assertThat(secret(properties.get("username")), not(is(true)));
        assertThat(secret(properties.get("token")), is(true));
        assertThat(secret(properties.get("oauthToken")), is(true));
    }

    // Dynamic-renderable non-String properties (e.g. Long, enum, List) render as an "anyOf" of type
    // variants (typed value + Pebble expression string) instead of a flat schema, so `$group`/`$secret`
    // live on the first "anyOf" entry rather than at the top level.
    private static Object group(Map<String, Object> propertySchema) {
        return metadata(propertySchema).get("$group");
    }

    private static Object secret(Map<String, Object> propertySchema) {
        return metadata(propertySchema).get("$secret");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> metadata(Map<String, Object> propertySchema) {
        if (propertySchema.containsKey("$group") || propertySchema.containsKey("$secret")) {
            return propertySchema;
        }
        var anyOf = (List<Map<String, Object>>) propertySchema.get("anyOf");
        return anyOf.getFirst();
    }
}
