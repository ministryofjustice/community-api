package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceSubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceType;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void typeMappedFromCircumstanceType() {
        assertThat(PersonalCircumstanceTransformer.personalCircumstanceOf(
                aPersonalCircumstance()
                        .toBuilder()
                        .circumstanceType(CircumstanceType.builder().codeValue("X").codeDescription("Description").build())
                        .build()).getPersonalCircumstanceType())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "X")
                .hasFieldOrPropertyWithValue("description", "Description");

    }

    @Test
    public void subTypeMappedFromCircumstanceSubType() {
        assertThat(PersonalCircumstanceTransformer.personalCircumstanceOf(
                aPersonalCircumstance()
                        .toBuilder()
                        .circumstanceSubType(CircumstanceSubType.builder().codeValue("X").codeDescription("Description").build())
                        .build()).getPersonalCircumstanceSubType())
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

    private PersonalCircumstance aPersonalCircumstance() {
        return PersonalCircumstance.builder()
                .circumstanceSubType(CircumstanceSubType.builder().codeValue("X").codeDescription("Description").build())
                .circumstanceType(CircumstanceType.builder().codeValue("X").codeDescription("Description").build())
                .build();
    }

}