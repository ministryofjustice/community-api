package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StandardReferenceTest {

    @Test
    void canToStringWithCircularReference() {

        var reference = StandardReference.builder().codeValue("XX").build();
        var master = ReferenceDataMaster.builder().standardReferences(List.of(reference)).build();
        reference.setReferenceDataMaster(master);

        assertThat(reference.toString()).contains("XX");
    }
}