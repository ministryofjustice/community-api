package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OffenderProfile {
    private String ethnicity;
    private String nationality;
    private String secondaryNationality;
    private String notes;
    private String immigrationStatus;
    private OffenderLanguages offenderLanguages;
    private String religion;
    private String sexualOrientation;
    private Map<String, String> offenderDetails;
    private String remandStatus;
    private Conviction previousConviction;
    private String riskColour;
}
