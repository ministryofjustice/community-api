package uk.gov.justice.digital.delius.jpa.standard.entity;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {
    @Nested
    class CPSPack {

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

    @Nested
    class IsActive {
        @Test
        public void isActiveWhenFlagIsOne() {
            assertThat(Event
                    .builder()
                    .activeFlag(1L)
                    .build().isActive()).isTrue();
        }

        @Test
        public void isNotActiveWhenFlagIsZero() {
            assertThat(Event
                    .builder()
                    .activeFlag(0L)
                    .build().isActive()).isFalse();
        }

        @Test
        public void isNotActiveWhenDeletedEvenWhenActiveFlagOne() {
            assertThat(Event
                    .builder()
                    .activeFlag(1L)
                    .softDeleted(1L)
                    .build().isActive()).isFalse();
        }

    }

}