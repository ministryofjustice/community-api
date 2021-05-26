package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.CaseAllocationRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.RegistrationRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskServiceTest {
    @Mock
    private CaseAllocationRepository caseAllocationRepository;
    @Mock
    private RegistrationRepository registrationRepository;

    private RiskService riskService;

    @BeforeEach
    void setUp() {
        riskService = new RiskService(registrationRepository, caseAllocationRepository);
    }

    @Nested
    @DisplayName("GetResourcingDetails")
    class GetResourcingDetails {
        @Test
        @DisplayName("will return resourcing details when found")
        void willReturnResourcingDetailsWhenFound() {
            when(caseAllocationRepository.findFirstByOffenderIdAndAllocationDecisionDateNotNullOrderByAllocationDecisionDateDesc(99L))
                .thenReturn(Optional
                    .of(CaseAllocation
                        .builder()
                        .allocationDecision(StandardReference
                            .builder()
                            .codeValue("R")
                            .codeDescription("Retained")
                            .build())
                        .allocationDecisionDate(LocalDateTime.parse("2020-07-19T10:00:00"))
                        .event(Event.builder().eventId(88L).build())
                        .build()));

            final var resourcingDetails = riskService.getResourcingDetails(99L);

            assertThat(resourcingDetails).isPresent();
        }

        @Test
        @DisplayName("will return empty when not found")
        void willReturnEmptyWhenNotFound() {
            when(caseAllocationRepository.findFirstByOffenderIdAndAllocationDecisionDateNotNullOrderByAllocationDecisionDateDesc(99L))
                .thenReturn(Optional.empty());
            final var resourcingDetails = riskService.getResourcingDetails(99L);

            assertThat(resourcingDetails).isEmpty();
        }

    }
}
