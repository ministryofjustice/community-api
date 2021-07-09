package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPersonalCircumstance;

public class PersonalCircumstanceTransformerTest {

    @Test
    public void probationAreasMappedOnlyWhenNotNull() {

        assertThat(PersonalCircumstanceTransformer.personalCircumstanceOf(
                aPersonalCircumstance()
                        .toBuilder()
                        .probationArea(null)
                        .build()).getProbationArea())
                .isNull();

        assertThat(PersonalCircumstanceTransformer.personalCircumstanceOf(
            aPersonalCircumstance()
                    .toBuilder()
                    .probationArea(ProbationArea.builder().code("X").description("Description").build())
                    .build()).getProbationArea())
            .isNotNull()
            .hasFieldOrPropertyWithValue("code", "X")
            .hasFieldOrPropertyWithValue("description", "Description");
    }

    @Test
    public void evidencedConvertedToBoolean() {
        assertThat(PersonalCircumstanceTransformer
                .personalCircumstanceOf(aPersonalCircumstance().toBuilder().evidenced("Y").build()).getEvidenced()).isTrue();
        assertThat(PersonalCircumstanceTransformer
                .personalCircumstanceOf(aPersonalCircumstance().toBuilder().evidenced("N").build()).getEvidenced()).isFalse();
        assertThat(PersonalCircumstanceTransformer
                .personalCircumstanceOf(aPersonalCircumstance().toBuilder().evidenced(null).build()).getEvidenced()).isNull();
    }

    @Test
    public void personalCircumstanceMapped() {
        final var source = aPersonalCircumstance();
        final var observed = PersonalCircumstanceTransformer.personalCircumstanceOf(source);
        assertThat(observed)
            .hasFieldOrPropertyWithValue("personalCircumstanceId", 1000L)
            .hasFieldOrPropertyWithValue("offenderId", 1001L)
            .hasFieldOrPropertyWithValue("startDate", LocalDate.of(2021, 7, 9))
            .hasFieldOrPropertyWithValue("endDate", LocalDate.of(2021, 7, 10))
            .hasFieldOrPropertyWithValue("notes", "Some notes")
            .hasFieldOrPropertyWithValue("evidenced", true)
            .hasFieldOrPropertyWithValue("personalCircumstanceType.code", "CT")
            .hasFieldOrPropertyWithValue("personalCircumstanceType.description", "AP - Medication in Posession - Assessment")
            .hasFieldOrPropertyWithValue("personalCircumstanceSubType.code", "CST")
            .hasFieldOrPropertyWithValue("personalCircumstanceSubType.description", "MiP approved")
            .hasFieldOrPropertyWithValue("createdDatetime", LocalDateTime.of(2021, 7, 9, 9, 12))
            .hasFieldOrPropertyWithValue("lastUpdatedDatetime", LocalDateTime.of(2021, 7, 9, 9, 32));
    }

}