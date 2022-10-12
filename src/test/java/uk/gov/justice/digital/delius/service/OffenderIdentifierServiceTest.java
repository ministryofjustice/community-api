package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalIdentifier;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private ArgumentCaptor<Offender> offenderCaptor = ArgumentCaptor.forClass(Offender.class);
    private ArgumentCaptor<AdditionalIdentifier> additionalIdentifierCaptor = ArgumentCaptor.forClass(AdditionalIdentifier.class);
    private ReferenceDataService referenceDataService = mock(ReferenceDataService.class);

    @Nested
    class FeatureSwitchedOff {
        @BeforeEach
        void setUp() {
            service = new OffenderIdentifierService(false, offenderRepository, referenceDataService);
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

        @Test
        void willNotReplaceNOMSNumber() {
            when(offenderRepository.findAllByNomsNumber("G5555TT")).thenReturn(List.of());
            when(offenderRepository.findAllByNomsNumber("A9999XX")).thenReturn(List.of(
                    Offender
                            .builder()
                            .crn("X12345")
                            .pncNumber("2018/0012345X")
                            .build()
            ));
            final var iDs = service
                    .replaceNomsNumber("A9999XX", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT").build())
                    .get(0);

            assertThat(iDs.getCrn()).isEqualTo("X12345");
            assertThat(iDs.getNomsNumber()).isNull();
            assertThat(iDs.getPncNumber()).isEqualTo("2018/0012345X");
        }
    }

    @Nested
    class FeatureSwitchedOn {
        @BeforeEach
        void setUp() {
            service = new OffenderIdentifierService(true, offenderRepository, referenceDataService);
            when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.empty());
        }

        @Nested
        class Update {

            @Nested
            class NoOffenderFound {
                @Test
                void willThrowNotFoundWhenOffenderNotFound() {
                    when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.empty());
                    assertThrows(NotFoundException.class, () -> service
                            .updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                                    .build()));
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
                            .updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                                    .build());

                    assertThat(iDs.getCrn()).isEqualTo("X12345");
                    assertThat(iDs.getNomsNumber()).isEqualTo("G5555TT");
                    assertThat(iDs.getPncNumber()).isEqualTo("2018/0012345X");
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
                    iDs = service.updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                            .build());
                }

                @Test
                void willUpdateOffender() {
                    assertThat(iDs.getNomsNumber()).isEqualTo("G5555TT");
                }

            }

            @Nested
            class AnotherNOMSAlreadyAssignedToOffender {
                private IDs iDs;

                @BeforeEach
                void setUp() {
                    when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of());
                    when(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(
                            Offender
                                    .builder()
                                    .offenderId(99L)
                                    .crn("X12345")
                                    .nomsNumber("A7777TT")
                                    .additionalIdentifiers(new ArrayList<>())
                                    .build()
                    ));
                    when(referenceDataService.formerNomsNumberAdditionalIdentifier()).thenReturn(StandardReference
                            .builder()
                            .codeValue("XNOMS")
                            .codeDescription("Former NOMIS Number")
                            .build());
                    iDs = service.updateNomsNumber("X12345", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                            .build());
                }

                @Test
                void willUpdateOffender() {
                    assertThat(iDs.getNomsNumber()).isEqualTo("G5555TT");
                }
            }
        }
        @Nested
        class Replace {
            @Nested
            class NoOffenderFound {
                @Test
                void willThrowNotFoundWhenOffenderNotFoundForOriginalNomsNumber() {
                    when(offenderRepository.findAllByNomsNumber("G5555TT")).thenReturn(List.of());
                    when(offenderRepository.findAllByNomsNumber("A9999XX")).thenReturn(List.of());

                    assertThrows(NotFoundException.class, () -> service
                            .replaceNomsNumber("A9999XX", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                                    .build()));
                }

            }

            @Nested
            class OffenderFound {
                private IDs iDs;

                @BeforeEach
                void setUp() {
                    when(offenderRepository.findAllByNomsNumber("G5555TT")).thenReturn(List.of());
                    when(offenderRepository.findAllByNomsNumber("A9999XX")).thenReturn(List.of(
                            Offender
                                    .builder()
                                    .offenderId(99L)
                                    .crn("X12345")
                                    .nomsNumber("A9999XX")
                                    .additionalIdentifiers(new ArrayList<>())
                                    .build()
                    ));
                    when(referenceDataService.formerNomsNumberAdditionalIdentifier()).thenReturn(StandardReference
                            .builder()
                            .codeValue("XNOMS")
                            .codeDescription("Former NOMIS Number")
                            .build());
                    iDs = service
                            .replaceNomsNumber("A9999XX", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                                    .build()).get(0);
                }

                @Test
                void willReturnUpdatedOffender() {
                    assertThat(iDs.getNomsNumber()).isEqualTo("G5555TT");
                    assertThat(iDs.getCrn()).isEqualTo("X12345");
                }
            }
            @Nested
            class MultipleOffendersFound {
                private List<IDs> iDs;

                @BeforeEach
                void setUp() {
                    when(offenderRepository.findAllByNomsNumber("G5555TT")).thenReturn(List.of());
                    when(offenderRepository.findAllByNomsNumber("A9999XX")).thenReturn(List.of(
                            Offender
                                    .builder()
                                    .offenderId(99L)
                                    .crn("X12345")
                                    .nomsNumber("A9999XX")
                                    .additionalIdentifiers(new ArrayList<>())
                                    .build(),
                            Offender
                                    .builder()
                                    .offenderId(99L)
                                    .crn("X34567")
                                    .nomsNumber("A9999XX")
                                    .additionalIdentifiers(new ArrayList<>())
                                    .build()
                    ));
                    when(referenceDataService.formerNomsNumberAdditionalIdentifier()).thenReturn(StandardReference
                            .builder()
                            .codeValue("XNOMS")
                            .codeDescription("Former NOMIS Number")
                            .build());
                    iDs = service
                            .replaceNomsNumber("A9999XX", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                                    .build());
                }

                @Test
                void willReturnAllUpdatedOffenders() {
                    assertThat(iDs.get(0).getNomsNumber()).isEqualTo("G5555TT");
                    assertThat(iDs.get(0).getCrn()).isEqualTo("X12345");
                    assertThat(iDs.get(1).getNomsNumber()).isEqualTo("G5555TT");
                    assertThat(iDs.get(1).getCrn()).isEqualTo("X34567");
                }
            }

            @Nested
            class NOMSAssignedToOtherOffenderAlready {
                private final Offender existingOffender = Offender
                        .builder()
                        .offenderId(88L)
                        .crn("X88888")
                        .nomsNumber("G5555TT")
                        .additionalIdentifiers(new ArrayList<>())
                        .build();

                @BeforeEach
                void setUp() {
                    when(offenderRepository.findAllByNomsNumber("G5555TT")).thenReturn(List.of(existingOffender));
                }

                @Test
                void willThrowBadRequestException() {
                    assertThatThrownBy(() -> service
                            .replaceNomsNumber("A9999XX", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                                    .build()))
                            .isInstanceOf(ConflictingRequestException.class)
                            .hasMessage("NOMS number G5555TT is already assigned to X88888");
                }

                @Test
                void willNotUpdateExistingOffenderWithNOMSNumber() {
                    assertThatThrownBy(() -> service
                            .replaceNomsNumber("A9999XX", UpdateOffenderNomsNumber.builder().nomsNumber("G5555TT")
                                    .build()));
                }
            }
        }
    }
}