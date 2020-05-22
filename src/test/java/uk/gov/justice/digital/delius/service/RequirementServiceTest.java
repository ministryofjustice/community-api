package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
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

    private RequirementService requirementService;

    @Before
    public void setUp() {
        requirementService = new RequirementService(offenderRepository, eventRepository);

        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Collections.singletonList(event));
        when(event.getDisposal()).thenReturn(disposal);
        when(event.getEventId()).thenReturn(CONVICTION_ID);
        when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement.builder().build()));
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