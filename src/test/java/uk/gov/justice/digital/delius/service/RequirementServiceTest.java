package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.data.api.PssRequirements;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequirementServiceTest {
    private static final Long CONVICTION_ID = 987654321L;
    private static final String CRN = "CRN";
    public static final Long OFFENDER_ID = 123456789L;

    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private Offender offender;
    @Mock
    private Event event;
    @Mock
    private Event badEvent;
    @Mock
    private Disposal disposal;
    @Mock
    private Custody custody;

    private RequirementService requirementService;

    @Before
    public void setUp() {
        requirementService = new RequirementService(offenderRepository, eventRepository);

        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Collections.singletonList(event));
        when(event.getDisposal()).thenReturn(disposal);
        when(event.getEventId()).thenReturn(CONVICTION_ID);
    }

    @Test
    public void whenGetPssRequirementsByConvictionId_thenReturnRequirements() {
        when(disposal.getCustody()).thenReturn(custody);
        when(custody.getPssRequirements()).thenReturn(Collections.singletonList(PssRequirement.builder()
                .pssRequirementId(88L)
                .pssRequirementTypeMainCategory(PssRequirementTypeMainCategory.builder()
                        .description("Standard 7 Conditions")
                        .code("A")
                        .build())
                .pssRequirementTypeSubCategory(StandardReference.builder()
                        .codeDescription("SubType")
                        .codeValue("B")
                        .build())
                .activeFlag(1L)
                .build()));
        PssRequirements requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getPssRequirements()).hasSize(1);
        uk.gov.justice.digital.delius.data.api.PssRequirement pssRequirement = requirements.getPssRequirements().get(0);

        assertThat(pssRequirement.getPssRequirementId()).isEqualTo(88L);
        assertThat(pssRequirement.getType().getDescription()).isEqualTo("Standard 7 Conditions");
        assertThat(pssRequirement.getType().getCode()).isEqualTo("A");
        assertThat(pssRequirement.getSubType().getDescription()).isEqualTo("SubType");
        assertThat(pssRequirement.getSubType().getCode()).isEqualTo("B");
        assertThat(pssRequirement.getActive()).isEqualTo(true);
    }

    @Test
    public void givenOffenderDoesNotExist_whenGetPssRequirementsByConvictionId_thenThrowException() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID))
                .withMessage("Offender with CRN 'CRN' not found");
    }

    @Test
    public void givenMultipleEventsReturnedForOffender_whenGetPssRequirementsByConvictionId_thenFilterByConvictionId() {
        when(disposal.getCustody()).thenReturn(custody);
        when(custody.getPssRequirements()).thenReturn(Collections.singletonList(PssRequirement.builder()
                .pssRequirementId(88L)
                .activeFlag(0L)
                .build()));
        when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

        PssRequirements requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getPssRequirements()).hasSize(1);
        assertThat(requirements.getPssRequirements().get(0).getPssRequirementId()).isEqualTo(88L);
        assertThat(requirements.getPssRequirements().get(0).getActive()).isEqualTo(false);
    }

    @Test
    public void givenNoDisposalForConviction_whenGetPssRequirementsByConvictionId_thenReturnEmptyList() {
        when(event.getDisposal()).thenReturn(null);

        PssRequirements requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getPssRequirements()).isEmpty();
    }

    @Test
    public void givenNoCustodyForConviction_whenGetPssRequirementsByConvictionId_thenReturnEmptyList() {
        when(disposal.getCustody()).thenReturn(custody);

        PssRequirements requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getPssRequirements()).isEmpty();
    }

    @Test
    public void givenNoPssRequirementsForConviction_whenGetPssRequirementsByConvictionId_thenReturnEmptyList() {
        when(disposal.getCustody()).thenReturn(custody);
        when(custody.getPssRequirements()).thenReturn(null);

        PssRequirements requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getPssRequirements()).isEmpty();
    }

    @Test
    public void whenGetRequirementsByConvictionId_thenReturnRequirements() {
        when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .build()));
        ConvictionRequirements requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getRequirements()).hasSize(1);
        assertThat(requirements.getRequirements().get(0).getRequirementId()).isEqualTo(99L);
    }

    @Test
    public void givenOffenderDoesNotExist_whenGetRequirementsByConvictionId_thenThrowException() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
            .isThrownBy(() -> requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID))
            .withMessage("Offender with CRN 'CRN' not found");
    }

    @Test
    public void givenMultipleEventsReturnedForOffender_whenGetRequirementsByConvictionId_thenFilterByConvictionId() {
        when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .build()));
        when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

        ConvictionRequirements requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getRequirements()).hasSize(1);
        assertThat(requirements.getRequirements().get(0).getRequirementId()).isEqualTo(99L);
    }

    @Test
    public void givenNoDisposalForConviction_whenGetRequirementsByConvictionId_thenReturnEmptyList() {
        when(event.getDisposal()).thenReturn(null);
        when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

        ConvictionRequirements requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getRequirements()).isEmpty();
    }

    @Test
    public void givenRequirementsForConviction_whenGetRequirementsByConvictionId_thenReturnEmptyList() {
        when(disposal.getRequirements()).thenReturn(null);
        when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

        ConvictionRequirements requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID);
        assertThat(requirements.getRequirements()).isEmpty();
    }
}