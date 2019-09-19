package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {
    @Test
    public void hasCpsPackWhenHasAlfrescoIdAndNotDeleted() {
        assertThat(Event
                .builder()
                .cpsAlfrescoDocumentId("1334")
                .cpsSoftDeleted(0L)
                .build().hasCpsPack()).isTrue();
    }
    @Test
    public void hasCpsPackWhenHasAlfrescoIdAndNotDeletedFlagAbsent() {
        assertThat(Event
                .builder()
                .cpsAlfrescoDocumentId("1334")
                .cpsSoftDeleted(null)
                .build().hasCpsPack()).isTrue();
    }
    @Test
    public void doesNotHaveCpsPackWhenHasAlfrescoIdAndNotDeletedTrue() {
        assertThat(Event
                .builder()
                .cpsAlfrescoDocumentId("1334")
                .cpsSoftDeleted(1L)
                .build().hasCpsPack()).isFalse();
    }

    @Test
    public void doesNotHaveCpsPackWhenDoesNotHaveAnAlfrescoId() {
        assertThat(Event
                .builder()
                .cpsAlfrescoDocumentId("")
                .cpsSoftDeleted(0L)
                .build().hasCpsPack()).isFalse();
    }

    @Test
    public void doesNotHaveCpsPackWhenAlfrescoIdAbsent() {
        assertThat(Event
                .builder()
                .cpsAlfrescoDocumentId(null)
                .cpsSoftDeleted(0L)
                .build().hasCpsPack()).isFalse();
    }

}