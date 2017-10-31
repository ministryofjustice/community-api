package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;


@Data
@Builder(toBuilder = true)
public class OffenderLanguages {
    private Optional<String> primaryLanguage;
    private List<String> otherLanguages;
    private Optional<String> languageConcerns;
    private boolean requiresInterpreter;
}
