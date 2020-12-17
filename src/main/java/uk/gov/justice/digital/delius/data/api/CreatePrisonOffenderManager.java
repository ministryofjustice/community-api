package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ApiModel(description = "Request body for assigning an offender manager to an offender. Must pass exactly one of officer / officerCode (not both)")
public class CreatePrisonOffenderManager {
    // This annotation appears to be ignored by swagger docs and nobody seems to care: https://github.com/springfox/springfox/issues/2237
    @ApiModelProperty(value = "Name of offender manager. If passed then must contain both forename(s) and surname", example = "officer: {\"forenames\": \"John\", \"surname\": \"Smith\" }")
    private ContactableHuman officer;
    @ApiModelProperty(value = "Officer staff ID. If not present officer will be used to lookup staff member", example = "1234567")
    private Long staffId;
    @ApiModelProperty(value = "Prison institution code in NOMIS", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;

    /**
     * This is tested in the API @see OffenderResources_AllocatePrisonOffenderManagerAPITest
     */
    public Optional<String> validate() {
        final var prisonCodeMissing = isBlank(getNomsPrisonInstitutionCode());
        final var staffIdExists = isNotEmpty(getStaffId());
        final var officerExists = isNotEmpty(getOfficer());
        final var officerForenamesMissing = officerExists && isBlank(getOfficer().getForenames());
        final var officerSurnamesMissing = officerExists && isBlank(getOfficer().getSurname());

        final String expectedToContain;
        if (prisonCodeMissing) {
            expectedToContain = "a NOMS prison institution code";
        } else if (!staffIdExists && !officerExists) {
            expectedToContain = "either officer or staff id";
        } else if (staffIdExists && officerExists) {
            expectedToContain = "either officer OR staff id, not both";
        } else if (officerExists && officerForenamesMissing && officerSurnamesMissing) {
            expectedToContain = "both officer names";
        } else if (officerExists && officerForenamesMissing) {
            expectedToContain = "an officer with forenames";
        } else if (officerExists && officerSurnamesMissing) {
            expectedToContain = "an officer with a surname";
        } else {
            expectedToContain = "";
        }

        if (!expectedToContain.isBlank()) {
            return Optional.of("Expected createPrisonOffenderManager to contain " + expectedToContain);
        }

        return Optional.empty();
    }

}
