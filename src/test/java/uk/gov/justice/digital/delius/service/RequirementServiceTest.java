package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.LicenceConditions;
import uk.gov.justice.digital.delius.data.api.PssRequirements;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceCondition;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceConditionTypeMainCat;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirementTypeSubCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequirementServiceTest {
    private static final Long CONVICTION_ID = 987654321L;
    private static final String CRN = "CRN";
    private static final String REHABILITATION_ACTIVITY_REQUIREMENT_TYPE = "F";
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

    @Nested
    class RequirementTests {
        @BeforeEach
        public void setUp() {
            requirementService = new RequirementService(offenderRepository, eventRepository);

            when(offenderRepository.getOffenderIdFrom(CRN)).thenReturn(Optional.of(OFFENDER_ID));
            when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Collections.singletonList(event));
            when(event.getDisposal()).thenReturn(disposal);
            when(event.getEventId()).thenReturn(CONVICTION_ID);
        }

        @Test
        public void whenGetLicenceConditionsByConvictionId_thenReturnLicenceConditions() {
            var commencementDate = LocalDate.of(2020, 9, 18);
            var startDate = LocalDate.of(2020, 9, 19);
            var terminatedDate = LocalDate.of(2020, 9, 20);
            var createdDateTime = LocalDateTime.of(2020, 9, 18, 1, 0);

            when(disposal.getLicenceConditions()).thenReturn(Collections.singletonList(LicenceCondition.builder()
                .licenceConditionId(88L)
                .licenceConditionTypeMainCat(LicenceConditionTypeMainCat.builder()
                    .description("Main Cat")
                    .code("A")
                    .build())
                .licenceConditionTypeSubCat(StandardReference.builder()
                    .codeDescription("Building Better Relationships (BBR)")
                    .codeValue("LC77")
                    .build())
                .commencementDate(commencementDate)
                .commencementNotes("Commencement notes")
                .startDate(startDate)
                .terminationDate(terminatedDate)
                .terminationNotes("Termination notes")
                .createdDateTime(createdDateTime)
                .activeFlag(1L)
                .build()));

            var conditions = requirementService.getLicenceConditionsByConvictionId(CRN, CONVICTION_ID);
            assertThat(conditions.getLicenceConditions()).hasSize(1);
            var licenceCondition = conditions.getLicenceConditions().get(0);

            assertThat(licenceCondition.getLicenceConditionTypeMainCat().getDescription()).isEqualTo("Main Cat");
            assertThat(licenceCondition.getLicenceConditionTypeMainCat().getCode()).isEqualTo("A");
            assertThat(licenceCondition.getLicenceConditionTypeSubCat().getDescription()).isEqualTo("Building Better Relationships (BBR)");
            assertThat(licenceCondition.getLicenceConditionTypeSubCat().getCode()).isEqualTo("LC77");
            assertThat(licenceCondition.getActive()).isEqualTo(true);
            assertThat(licenceCondition.getCommencementDate()).isEqualTo(commencementDate);
            assertThat(licenceCondition.getCommencementNotes()).isEqualTo("Commencement notes");
            assertThat(licenceCondition.getCreatedDateTime()).isEqualTo(createdDateTime);
            assertThat(licenceCondition.getStartDate()).isEqualTo(startDate);
            assertThat(licenceCondition.getTerminationDate()).isEqualTo(terminatedDate);
            assertThat(licenceCondition.getTerminationNotes()).isEqualTo("Termination notes");
        }

        @Test
        public void givenMultipleEventsReturnedForOffender_whenGetLicenceConditionsByConvictionId_thenFilterByConvictionId() {
            when(disposal.getLicenceConditions()).thenReturn(Collections.singletonList(LicenceCondition.builder()
                .commencementNotes("Commencement notes")
                .build()));
            when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

            LicenceConditions requirements = requirementService.getLicenceConditionsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getLicenceConditions()).hasSize(1);
            assertThat(requirements.getLicenceConditions().get(0).getCommencementNotes()).isEqualTo("Commencement notes");
        }

        @Test
        public void givenNoDisposalForConviction_whenGetLicenceConditionsByConvictionId_thenReturnEmptyList() {
            when(event.getDisposal()).thenReturn(null);

            LicenceConditions requirements = requirementService.getLicenceConditionsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getLicenceConditions()).isEmpty();
        }

        @Test
        public void givenNoLicenceConditionsForConviction_whenGetLicenceConditionsByConvictionId_thenReturnEmptyList() {
            when(disposal.getLicenceConditions()).thenReturn(null);

            var requirements = requirementService.getLicenceConditionsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getLicenceConditions()).isEmpty();
        }

        @Test
        public void whenGetPssRequirementsByConvictionId_thenReturnPssRequirements() {
            when(disposal.getCustody()).thenReturn(custody);
            when(custody.getPssRequirements()).thenReturn(Collections.singletonList(PssRequirement.builder()
                .pssRequirementId(88L)
                .pssRequirementTypeMainCategory(PssRequirementTypeMainCategory.builder()
                    .description("Standard 7 Conditions")
                    .code("A")
                    .build())
                .pssRequirementTypeSubCategory(PssRequirementTypeSubCategory.builder()
                    .description("SubType")
                    .code("B")
                    .build())
                .activeFlag(1L)
                .build()));
            var requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getPssRequirements()).hasSize(1);
            var pssRequirement = requirements.getPssRequirements().get(0);

            assertThat(pssRequirement.getType().getDescription()).isEqualTo("Standard 7 Conditions");
            assertThat(pssRequirement.getType().getCode()).isEqualTo("A");
            assertThat(pssRequirement.getSubType().getDescription()).isEqualTo("SubType");
            assertThat(pssRequirement.getSubType().getCode()).isEqualTo("B");
            assertThat(pssRequirement.getActive()).isEqualTo(true);
        }

        @Test
        public void givenMultipleEventsReturnedForOffender_whenGetPssRequirementsByConvictionId_thenFilterByConvictionId() {
            when(disposal.getCustody()).thenReturn(custody);
            when(custody.getPssRequirements()).thenReturn(Collections.singletonList(PssRequirement.builder()
                .pssRequirementTypeMainCategory(PssRequirementTypeMainCategory.builder()
                    .description("Standard 7 Conditions")
                    .code("A")
                    .build())
                .activeFlag(0L)
                .build()));
            when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

            PssRequirements requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getPssRequirements()).hasSize(1);
            assertThat(requirements.getPssRequirements().get(0).getType().getDescription()).isEqualTo("Standard 7 Conditions");
            assertThat(requirements.getPssRequirements().get(0).getType().getCode()).isEqualTo("A");
            assertThat(requirements.getPssRequirements().get(0).getActive()).isEqualTo(false);
        }

        @Test
        public void givenNoDisposalForConviction_whenGetPssRequirementsByConvictionId_thenReturnEmptyList() {
            when(event.getDisposal()).thenReturn(null);

            var requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getPssRequirements()).isEmpty();
        }

        @Test
        public void givenNoCustodyForConviction_whenGetPssRequirementsByConvictionId_thenReturnEmptyList() {
            when(disposal.getCustody()).thenReturn(custody);

            var requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getPssRequirements()).isEmpty();
        }

        @Test
        public void givenNoPssRequirementsForConviction_whenGetPssRequirementsByConvictionId_thenReturnEmptyList() {
            when(disposal.getCustody()).thenReturn(custody);
            when(custody.getPssRequirements()).thenReturn(null);

            var requirements = requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID);
            assertThat(requirements.getPssRequirements()).isEmpty();
        }

        @Test
        public void whenGetRequirementsByConvictionId_thenReturnRequirements() {
            when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .build()));
            var requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID, true, true);
            assertThat(requirements.getRequirements()).hasSize(1);
            assertThat(requirements.getRequirements().get(0).getRequirementId()).isEqualTo(99L);
        }

        @Test
        public void whenGetReferralRequirementByConvictionId_thenReturnRequirement() {
            when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .activeFlag(true)
                .softDeleted(false)
                .requirementTypeMainCategory(RequirementTypeMainCategory.builder().code("F").build())
                .build()));
            var requirement = requirementService.getActiveRequirement(CRN, CONVICTION_ID, REHABILITATION_ACTIVITY_REQUIREMENT_TYPE);
            assertThat(requirement.orElse(null).getRequirementId()).isEqualTo(99L);
        }

        @Test
        public void whenGetReferralRequirementByConvictionId_AndRequirementNotMatchingCategory_thenNoMatch() {
            when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .activeFlag(true)
                .softDeleted(false)
                .requirementTypeMainCategory(RequirementTypeMainCategory.builder().code("X").build())
                .build()));

            assertThat(requirementService.getActiveRequirement(CRN, CONVICTION_ID, REHABILITATION_ACTIVITY_REQUIREMENT_TYPE)).isEmpty();
        }

        @Test
        public void whenGetReferralRequirementByConvictionId_AndRequirementNotActive_thenNoMatch() {
            when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .activeFlag(false)
                .requirementTypeMainCategory(RequirementTypeMainCategory.builder().code("F").build())
                .build()));

            assertThat(requirementService.getActiveRequirement(CRN, CONVICTION_ID, REHABILITATION_ACTIVITY_REQUIREMENT_TYPE)).isEmpty();
        }

        @Test
        public void whenGetReferralRequirementByConvictionId_AndRequirementSoftDeleted_thenNoMatch() {
            when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .activeFlag(true)
                .softDeleted(true)
                .requirementTypeMainCategory(RequirementTypeMainCategory.builder().code("F").build())
                .build()));

            assertThat(requirementService.getActiveRequirement(CRN, CONVICTION_ID, REHABILITATION_ACTIVITY_REQUIREMENT_TYPE)).isEmpty();
        }

        @Test
        public void whenGetReferralRequirementByConvictionId_AndMultipleRequirementsExist_thenSelectLatest() {
            LocalDateTime now = LocalDateTime.now();
            when(disposal.getRequirements()).thenReturn(Arrays.asList(
                Requirement.builder().requirementId(99L).activeFlag(true).softDeleted(false)
                    .startDate(now.minusDays(1).toLocalDate()).createdDatetime(now)
                    .requirementTypeMainCategory(RequirementTypeMainCategory.builder().code("F").build()).build(),
                Requirement.builder().requirementId(100L).activeFlag(true).softDeleted(false)
                    .startDate(now.toLocalDate()).createdDatetime(now.plusHours(2))
                    .requirementTypeMainCategory(RequirementTypeMainCategory.builder().code("F").build()).build(),
                Requirement.builder().requirementId(101L).activeFlag(true).softDeleted(false)
                    .startDate(now.toLocalDate()).createdDatetime(now.plusHours(1))
                    .requirementTypeMainCategory(RequirementTypeMainCategory.builder().code("F").build()).build())
            );

            Optional<uk.gov.justice.digital.delius.data.api.Requirement> activeRequirement = requirementService.getActiveRequirement(CRN, CONVICTION_ID, REHABILITATION_ACTIVITY_REQUIREMENT_TYPE);
            assertThat(activeRequirement.get().getRequirementId()).isEqualTo(100L);
        }

        @Test
        public void whenGetReferralRequirementByConvictionId_AndNoRequirementsExist_thenReturnEmptyOptional() {
            when(disposal.getRequirements()).thenReturn(Collections.emptyList());

            assertThat(requirementService.getActiveRequirement(CRN, CONVICTION_ID, REHABILITATION_ACTIVITY_REQUIREMENT_TYPE)).isEmpty();
        }

        @Test
        public void givenMultipleEventsReturnedForOffender_whenGetRequirementsByConvictionId_thenFilterByConvictionId() {
            when(disposal.getRequirements()).thenReturn(Collections.singletonList(Requirement
                .builder()
                .requirementId(99L)
                .build()));
            when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

            var requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID, true, true);
            assertThat(requirements.getRequirements()).hasSize(1);
            assertThat(requirements.getRequirements().get(0).getRequirementId()).isEqualTo(99L);
        }

        @Test
        public void givenNoDisposalForConviction_whenGetRequirementsByConvictionId_thenReturnEmptyList() {
            when(event.getDisposal()).thenReturn(null);
            when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

            var requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID, true, true);
            assertThat(requirements.getRequirements()).isEmpty();
        }

        @Test
        public void givenRequirementsForConviction_whenGetRequirementsByConvictionId_thenReturnEmptyList() {
            when(disposal.getRequirements()).thenReturn(null);
            when(eventRepository.findByOffenderId(OFFENDER_ID)).thenReturn(Arrays.asList(event, badEvent));

            var requirements = requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID, true, true);
            assertThat(requirements.getRequirements()).isEmpty();
        }
    }

    @Nested class OffenderDoesNotExistTests {

        @BeforeEach
        public void setUp() {
            requirementService = new RequirementService(offenderRepository, eventRepository);
            when(offenderRepository.getOffenderIdFrom(CRN)).thenReturn(Optional.empty());
        }

        @Test
        public void givenOffenderDoesNotExist_whenGetLicenceConditionsByConvictionId_thenThrowException() {

            assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> requirementService.getLicenceConditionsByConvictionId(CRN, CONVICTION_ID))
                .withMessage("Offender with CRN 'CRN' not found");
        }

        @Test
        public void givenOffenderDoesNotExist_whenGetRequirementsByConvictionId_thenThrowException() {

            assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> requirementService.getRequirementsByConvictionId(CRN, CONVICTION_ID, true, true))
                .withMessage("Offender with CRN 'CRN' not found");
        }

        @Test
        public void givenOffenderDoesNotExist_whenGetPssRequirementsByConvictionId_thenThrowException() {

            assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> requirementService.getPssRequirementsByConvictionId(CRN, CONVICTION_ID))
                .withMessage("Offender with CRN 'CRN' not found");
        }
    }
}
