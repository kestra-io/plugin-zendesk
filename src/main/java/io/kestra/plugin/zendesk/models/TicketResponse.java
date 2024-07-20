package io.kestra.plugin.zendesk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketResponse {

    private Ticket ticket;

    private Ticket audit;

}
