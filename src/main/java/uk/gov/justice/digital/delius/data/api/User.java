package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Schema(required = true)
    private Long userId;
    private Long staffId;
    private String forename;
    private String forename2;
    private LocalDate endDate;
    private String surname;
    private String distinguishedName;
    private String externalProviderEmployeeFlag;
    private Long externalProviderId;
    private Long privateFlag;
    private Long organisationId;
    private Long scProviderId;
    private List<String> probationAreaCodes;

}
