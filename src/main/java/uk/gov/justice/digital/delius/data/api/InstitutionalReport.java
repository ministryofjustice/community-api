package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder(toBuilder = true)
public class InstitutionalReport {
    private Long institutionalReportId;
    private Long offenderId;
    private Conviction conviction;

    @ApiModelProperty(notes = "Deprecated - Use conviction to access sentence")
    public Sentence getSentence() {
        return Optional.ofNullable(conviction).map(Conviction::getSentence).orElse(null);
    }
}
