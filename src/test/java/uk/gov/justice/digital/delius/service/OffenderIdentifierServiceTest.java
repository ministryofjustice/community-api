package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OffenderIdentifierServiceTest {
    private OffenderIdentifierService service;
    private OffenderRepository offenderRepository = mock(OffenderRepository.class);
    private SpgNotificationService spgNotificationService = mock(SpgNotificationService.class);
    private ArgumentCaptor<Offender> offenderCaptor = ArgumentCaptor.forClass(Offender.class);
    private ReferenceDataService referenceDataService = mock(ReferenceDataService.class);

    @Nested
    class FeatureSwitchedOff {
        @BeforeEach
        void setUp() {
            service = new OffenderIdentifierService(false, new OffenderTransformer(new ContactTransformer()), offenderRepository, spgNotificationService, referenceDataService);
        }

        @Test
        void willNotUpdateNOMSNumber() {
            when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(
                    Offender
                            .builder()
                            .crn("X12345")
                            .pncNumber("2018/0012345X")
                            .build()
            ));
            final var iDs = service
                    .updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build());

            assertThat(iDs.getCrn()).isEqualTo("X12345");
            assertThat(iDs.getNomsNumber()).isNull();
            assertThat(iDs.getPncNumber()).isEqualTo("2018/0012345X");
        }
    }

    @Nested
    class FeatureSwitchedOn {
        @BeforeEach
        void setUp() {
            service = new OffenderIdentifierService(true, new OffenderTransformer(new ContactTransformer()), offenderRepository, spgNotificationService, referenceDataService);
            when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.empty());
        }

        @Nested
        class NoOffenderFound {
            @Test
            void willThrowNotFoundWhenOffenderNotFound() {
                when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.empty());
                assertThrows(NotFoundException.class, () -> service
                        .updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build()));
            }

        }

        @Nested
        class NewNOMSNumber {
            @BeforeEach
            void setUp() {
                when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of());
            }

            @Test
            void willReturnUpdatedIDsWhenAllOK() {
                when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(
                        Offender
                                .builder()
                                .crn("X12345")
                                .pncNumber("2018/0012345X")
                                .build()
                ));
                final var iDs = service
                        .updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build());

                assertThat(iDs.getCrn()).isEqualTo("X12345");
                assertThat(iDs.getNomsNumber()).isEqualTo("G5555TT");
                assertThat(iDs.getPncNumber()).isEqualTo("2018/0012345X");
            }

            @Test
            void willUpdateOffender() {
                when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(
                        Offender
                                .builder()
                                .crn("X12345")
                                .pncNumber("2018/0012345X")
                                .build()
                ));
                final var IDs = service.updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build());

                assertThat(IDs.getNomsNumber()).isEqualTo("G5555TT");
            }

            @Test
            void willNotifySPGOfOffenderChanges() {
                var offender = Offender
                        .builder()
                        .crn("X12345")
                        .pncNumber("2018/0012345X")
                        .build();
                when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(offender));
                service.updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build());

                verify(spgNotificationService).notifyUpdateOfOffender(offender);
            }
        }

        @Nested
        class NewNOMSNumberAlreadySet {
            @BeforeEach
            void setUp() {
                when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of());
            }

            @Test
            void willNotNotifySPG() {
                final Offender offender = Offender
                        .builder()
                        .crn("X12345")
                        .pncNumber("2018/0012345X")
                        .nomsNumber("G5555TT")
                        .build();
                when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(offender));
                service.updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build());

                verify(spgNotificationService, never()).notifyUpdateOfOffender(offender);
            }

        }

        @Nested
        class NOMSAssignedToOtherOffender {
            private final Offender duplicateOffender = Offender
                    .builder()
                    .offenderId(88L)
                    .crn("X88888")
                    .nomsNumber("G5555TT")
                    .additionalIdentifiers(new ArrayList<>())
                    .build();

            private IDs iDs;

            @BeforeEach
            void setUp() {
                when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of(duplicateOffender));
                when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(
                        Offender
                                .builder()
                                .offenderId(99L)
                                .crn("X12345")
                                .build()
                ));
                when(referenceDataService.duplicateNomsNumberAdditionalIdentifier()).thenReturn(StandardReference
                        .builder()
                        .codeValue("DNOMS")
                        .codeDescription("Duplicate NOMIS Number")
                        .build());
                iDs = service.updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build());
            }

            @Test
            void willUpdateOffender() {
                assertThat(iDs.getNomsNumber()).isEqualTo("G5555TT");
            }

            @Test
            void willUpdateDuplicateOffender() {
                verify(spgNotificationService, times(2)).notifyUpdateOfOffender(offenderCaptor.capture());

                // can only test this via spg call since this is a side affect
                final Offender duplicateOffender = offenderCaptor.getAllValues().get(0);
                assertThat(duplicateOffender.getOffenderId()).isEqualTo(88L);
                assertThat(duplicateOffender.getNomsNumber()).isNull();
                assertThat(duplicateOffender.getAdditionalIdentifiers()).hasSize(1);
                assertThat(duplicateOffender.getAdditionalIdentifiers().get(0).getIdentifierName().getCodeValue()).isEqualTo("DNOMS");
            }

            @Test
            void willNotifySPGForDuplicateOffender() {
                verify(spgNotificationService, times(2)).notifyUpdateOfOffender(offenderCaptor.capture());
                final Offender duplicateOffender = offenderCaptor.getAllValues().get(0);
                assertThat(duplicateOffender.getOffenderId()).isEqualTo(88L);
            }
        }
    }
}