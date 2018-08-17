package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"code", "description"})
public class OffenceDetail {
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
