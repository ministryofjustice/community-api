package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ApiModel(description = "Request body for assigning an offender manager to an offender. Must pass exactly one of officer / officerCode (not both)")
public class CreatePrisonOffenderManager {
    // This annotation appears to be ignored by swagger docs and nobody seems to care: https://github.com/springfox/springfox/issues/2237
    @ApiModelProperty(value = "Name of offender manager. If passed then must contain both forename(s) and surname", example = "officer: {\"forenames\": \"John\", \"surname\": \"Smith\" }")
    private Human officer;
    @ApiModelProperty(value = "Officer staff code. If not present officer will be used to lookup staff member", example = "N07A001")
    private String officerCode;
    @ApiModelProperty(value = "Prison institution code in NOMIS", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;

    /**
     * This is tested in the API @see OffenderResources_AllocatePrisonOffenderManagerAPITest
     */
    public String validate() {
        final var prisonCodeMissing = isNullOrEmpty(getNomsPrisonInstitutionCode());
        final var officerCodeExists = !isNullOrEmpty(getOfficerCode());
        final var officerExists = !isNull(getOfficer());
        final var officerForenamesMissing = officerExists && isNullOrEmpty(getOfficer().getForenames());
        final var officerSurnamesMissing = officerExists && isNullOrEmpty(getOfficer().getSurname());

        var expectedToContain = "";
        if (prisonCodeMissing) {
            expectedToContain = "a NOMS prison institution code";
        }
        else if (!officerCodeExists && !officerExists) {
            expectedToContain =  "either officer or officer code";
        }
        else if (officerCodeExists && officerExists) {
            expectedToContain = "either officer OR officer code";
        }
        else if (officerExists && officerForenamesMissing && officerSurnamesMissing) {
            expectedToContain = "both officer names";
        }
        else if (officerExists && officerForenamesMissing) {
            expectedToContain = "an officer with forenames";
        }
        else if (officerExists && officerSurnamesMissing) {
            expectedToContain = "an officer with a surname";
        }

        if (!expectedToContain.isBlank()) {
            expectedToContain = "Expected createPrisonOffenderManager to contain " + expectedToContain;
        }

        return expectedToContain;
    }

}
