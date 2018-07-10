package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder(toBuilder = true)
public class OffenderLanguages {
    private String primaryLanguage;
    private List<String> otherLanguages;
    private String languageConcerns;
    private Boolean requiresInterpreter;
}
