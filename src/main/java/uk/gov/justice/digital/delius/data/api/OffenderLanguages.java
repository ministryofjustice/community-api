package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OffenderLanguages {
    private String primaryLanguage;
    private List<String> otherLanguages;
    private String languageConcerns;
    private Boolean requiresInterpreter;
}
