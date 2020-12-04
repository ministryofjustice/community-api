package uk.gov.justice.digital.delius.transformers;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceCondition;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceConditionTypeMainCat;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import static org.assertj.core.api.Assertions.assertThat;

class ContactTransformerTest {

    @DisplayName("Transform entity LicenceCondition to API equivalent")
    @Test
    void whenLicenceConditionsSet_thenTransformToApi() {
        var commencementDate = LocalDate.of(2020, Month.AUGUST, 1);
        var startDate = LocalDate.of(2020, Month.JULY, 1);
        var terminationDate = LocalDate.of(2020, Month.AUGUST, 1);
        var createdTime = startDate.atTime(9, 0);

        var licenceCondition = ContactTransformer.licenceConditionOf(LicenceCondition.builder()
            .activeFlag(1L)
            .commencementDate(commencementDate)
            .commencementNotes("comm notes")
            .createdDateTime(createdTime)
            .licenceConditionNotes("lc notes")
            .licenceConditionTypeMainCat(LicenceConditionTypeMainCat.builder().code("code").description("desc").build())
            .licenceConditionTypeSubCat(StandardReference.builder().codeValue("sub code").codeDescription("sub desc").build())
            .startDate(startDate)
            .terminationDate(terminationDate)
            .terminationNotes("tm notes")
            .build());

        assertThat(licenceCondition.getActive()).isTrue();
        assertThat(licenceCondition.getCommencementDate()).isEqualTo(commencementDate);
        assertThat(licenceCondition.getCommencementNotes()).isEqualTo("comm notes");
        assertThat(licenceCondition.getCreatedDateTime()).isEqualTo(createdTime);
        assertThat(licenceCondition.getLicenceConditionNotes()).isEqualTo("lc notes");
        assertThat(licenceCondition.getLicenceConditionTypeMainCat().getDescription()).isEqualTo("desc");
        assertThat(licenceCondition.getLicenceConditionTypeMainCat().getCode()).isEqualTo("code");
        assertThat(licenceCondition.getLicenceConditionTypeSubCat().getDescription()).isEqualTo("sub desc");
        assertThat(licenceCondition.getLicenceConditionTypeSubCat().getCode()).isEqualTo("sub code");
        assertThat(licenceCondition.getStartDate()).isEqualTo(startDate);
        assertThat(licenceCondition.getTerminationDate()).isEqualTo(terminationDate);
        assertThat(licenceCondition.getTerminationNotes()).isEqualTo("tm notes");
    }

    @DisplayName("Transform entity LicenceCondition with null content to API equivalent")
    @Test
    void givenNulls_whenTransform_thenTransformToApi() {
        var startDate = LocalDate.of(2020, Month.JULY, 1);

        var licenceCondition = ContactTransformer.licenceConditionOf(LicenceCondition.builder()
            .activeFlag(0L)
            .startDate(startDate)
            .build());

        assertThat(licenceCondition.getActive()).isFalse();
        assertThat(licenceCondition.getLicenceConditionTypeSubCat()).isNull();
        assertThat(licenceCondition.getLicenceConditionTypeMainCat()).isNull();
        assertThat(licenceCondition.getStartDate()).isEqualTo(startDate);
    }
}
