package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderReleasedNotification {
    @NotBlank(message = "Missing a NOMS prison institution code in nomsPrisonInstitutionCode")
    @ApiModelProperty(value = "The Prison institution code in NOMIS the offender was released from", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;

    @NotBlank(message = "Missing a NOMS release reason code")
    @ApiModelProperty(value = "The release reason code in NOMIS", required = true, example = "RELEASE")
    private String reason;

    @ApiModelProperty(value = "The date the offender was released from custody", example = "2020-10-25")
    private LocalDate releaseDate;
}
