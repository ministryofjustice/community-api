package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OffenceDetail {
    private Long id;
    private String code;
    private String description;
    private String abbreviation;
    private String mainCategoryCode;
    private String mainCategoryDescription;
    private String mainCategoryAbbreviation;
    private String ogrsOffenceCategory;
    private String subCategoryCode;
    private String subCategoryDescription;
    private String form20Code;
    private String subCategoryAbbreviation;
    private String cjitCode;
}
