package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class OffenderProfile {
    private Optional<String> ethnicity;
    private Optional<String> nationality;
    private Optional<String> secondaryNationality;
    private Optional<String> notes;
    private Optional<String> immigrationStatus;
    private OffenderLanguages offenderLanguages;
    private Optional<String> religion;
    private Optional<String> sexualOrientation;
    private Map<String, String> offenderDetails;
    private Optional<String> remandStatus;
    private Conviction previousConviction;


}
