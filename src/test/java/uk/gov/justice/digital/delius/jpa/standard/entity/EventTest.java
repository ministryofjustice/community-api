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
                    .cpsSoftDeleted(false)
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
                    .cpsSoftDeleted(true)
                    .build().hasCpsPack()).isFalse();
        }

        @Test
        public void doesNotHaveCpsPackWhenDoesNotHaveAnAlfrescoId() {
            assertThat(Event
                    .builder()
                    .cpsAlfrescoDocumentId("")
                    .cpsSoftDeleted(false)
                    .build().hasCpsPack()).isFalse();
        }

        @Test
        public void doesNotHaveCpsPackWhenAlfrescoIdAbsent() {
            assertThat(Event
                    .builder()
                    .cpsAlfrescoDocumentId(null)
                    .cpsSoftDeleted(false)
                    .build().hasCpsPack()).isFalse();
        }
    }

    @Nested
    class IsActive {
        @Test
        public void isActiveWhenFlagIsOne() {
            assertThat(Event
                    .builder()
                    .activeFlag(true)
                    .build().isActive()).isTrue();
        }

        @Test
        public void isNotActiveWhenFlagIsZero() {
            assertThat(Event
                    .builder()
                    .activeFlag(false)
                    .build().isActive()).isFalse();
        }

        @Test
        public void isNotActiveWhenDeletedEvenWhenActiveFlagOne() {
            assertThat(Event
                    .builder()
                    .activeFlag(true)
                    .softDeleted(true)
                    .build().isActive()).isFalse();
        }

    }

}
