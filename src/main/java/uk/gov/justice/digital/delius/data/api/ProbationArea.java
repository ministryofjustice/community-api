package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProbationArea {
    private Long probationAreaId;
    private String code;
    private String description;
    private KeyValue organisation;
    private Institution institution;
    private List<AllTeam> teams;
}
