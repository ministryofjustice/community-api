package uk.gov.justice.digital.delius.transformers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceSubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceType;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonalCircumstanceTransformerTest {
    private PersonalCircumstanceTransformer transformer;

    @Before
    public void before() {
        transformer = new PersonalCircumstanceTransformer();
    }

    @Test
    public void probationAreasMappedOnlyWhenNotNull() {

        assertThat(transformer.personalCircumstanceOf(
                aPersonalCircumstance()
                        .toBuilder()
                        .probationArea(null)
                        .build()).getProbationArea())
                .isNull();

        assertThat(transformer.personalCircumstanceOf(
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
        assertThat(transformer.personalCircumstanceOf(
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
        assertThat(transformer.personalCircumstanceOf(
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
        assertThat(transformer.personalCircumstanceOf(aPersonalCircumstance().toBuilder().evidenced("Y").build()).getEvidenced()).isTrue();
        assertThat(transformer.personalCircumstanceOf(aPersonalCircumstance().toBuilder().evidenced("N").build()).getEvidenced()).isFalse();
        assertThat(transformer.personalCircumstanceOf(aPersonalCircumstance().toBuilder().evidenced(null).build()).getEvidenced()).isNull();
    }

    private PersonalCircumstance aPersonalCircumstance() {
        return PersonalCircumstance.builder()
                .circumstanceSubType(CircumstanceSubType.builder().codeValue("X").codeDescription("Description").build())
                .circumstanceType(CircumstanceType.builder().codeValue("X").codeDescription("Description").build())
                .build();
    }

}