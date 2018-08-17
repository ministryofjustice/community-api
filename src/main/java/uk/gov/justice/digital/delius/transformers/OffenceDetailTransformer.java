package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OffenceDetail;

public class OffenceDetailTransformer {
    public static OffenceDetail detailOf(uk.gov.justice.digital.delius.jpa.standard.entity.Offence offence) {
        return OffenceDetail.builder()
            .description(offence.getDescription())
            .code(offence.getCode())
            .abbreviation(offence.getAbbreviation())
            .cjitCode(offence.getCjitCode())
            .form20Code(offence.getForm20Code())
            .mainCategoryDescription(offence.getMainCategoryDescription())
            .mainCategoryAbbreviation(offence.getMainCategoryAbbreviation())
            .mainCategoryCode(offence.getMainCategoryCode())
            .ogrsOffenceCategory(offence.getOgrsOffenceCategory().getCodeDescription())
            .subCategoryAbbreviation(offence.getSubCategoryAbbreviation())
            .subCategoryDescription(offence.getSubCategoryDescription())
            .subCategoryCode(offence.getSubCategoryCode())
            .build();
    }

}
