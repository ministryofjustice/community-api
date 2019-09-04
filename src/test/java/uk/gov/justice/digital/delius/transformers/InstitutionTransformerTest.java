package uk.gov.justice.digital.delius.transformers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import static org.assertj.core.api.Assertions.assertThat;

public class InstitutionTransformerTest {
    private InstitutionTransformer institutionTransformer;

    @Before
    public void before() {
        institutionTransformer = new InstitutionTransformer();
    }

    @Test
    public void isEstablishmentIsConvertedToBoolean() {
        assertThat(institutionTransformer.institutionOf(
                anInstitution().toBuilder().establishment("Y").build()
        ).getIsEstablishment()).isTrue();

        assertThat(institutionTransformer.institutionOf(
                anInstitution().toBuilder().establishment("N").build()
        ).getIsEstablishment()).isFalse();
    }

    @Test
    public void isPrivateIsConvertedToBoolean() {
        assertThat(institutionTransformer.institutionOf(
                anInstitution().toBuilder().privateFlag(1L).build()
        ).getIsPrivate()).isTrue();

        assertThat(institutionTransformer.institutionOf(
                anInstitution().toBuilder().privateFlag(0L).build()
        ).getIsPrivate()).isFalse();
    }

    @Test
    public void establishmentTypeConvertedToKeyValuePair() {
        assertThat(institutionTransformer.institutionOf(
                anInstitution().toBuilder().establishmentType(StandardReference
                        .builder()
                        .codeValue("XX")
                        .codeDescription("PRISON")
                        .build()).build()
        ).getEstablishmentType())
                .hasFieldOrPropertyWithValue("code", "XX")
                .hasFieldOrPropertyWithValue("description", "PRISON");

    }


    private RInstitution anInstitution() {
        return RInstitution.builder().build();
    }
}