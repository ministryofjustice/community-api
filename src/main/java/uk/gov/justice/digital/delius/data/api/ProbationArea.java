package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbationArea {
    @ApiModelProperty(required = true)
    private Long probationAreaId;
    private String code;
    private String description;
    private Boolean nps;
    private KeyValue organisation;
    private Institution institution;
    private List<AllTeam> teams;
}
