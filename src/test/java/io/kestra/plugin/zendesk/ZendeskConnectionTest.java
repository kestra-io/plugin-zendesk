package io.kestra.plugin.zendesk;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ZendeskConnectionTest {

    @Test
    void shouldAddTheSchemeAndDropAnyTrailingSlash() {
        assertThat(ZendeskConnection.normaliseBase("acme.zendesk.com"), is("https://acme.zendesk.com"));
        assertThat(ZendeskConnection.normaliseBase("acme.zendesk.com/"), is("https://acme.zendesk.com"));
        assertThat(ZendeskConnection.normaliseBase("http://localhost:8080/"), is("http://localhost:8080"));
    }
}
