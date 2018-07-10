package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class User {
    @ApiModelProperty(required = true)
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
