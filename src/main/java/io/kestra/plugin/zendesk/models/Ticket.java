package io.kestra.plugin.zendesk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticket {

    private Long id;

    private String url;

    private String subject;

    private String description;

    private String priority;

    private String type;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    private List<String> tags;

}
