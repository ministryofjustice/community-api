package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProbationArea {
    @ApiModelProperty(required = true)
    private Long probationAreaId;
    private String code;
    private String description;
    private KeyValue organisation;
    private Institution institution;
    private List<AllTeam> teams;
}
