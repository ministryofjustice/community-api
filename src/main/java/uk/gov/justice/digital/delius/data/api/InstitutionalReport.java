package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionalReport {
    private Long institutionalReportId;
    private Long offenderId;
    private Conviction conviction;

    @JsonIgnore
    @ApiModelProperty(notes = "Deprecated - Use conviction to access sentence")
    public Sentence getSentence() {
        return Optional.ofNullable(conviction).map(Conviction::getSentence).orElse(null);
    }
}
