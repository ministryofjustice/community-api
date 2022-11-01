package uk.gov.justice.digital.delius.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralEndRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralStartRequest;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiManager;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsiManager;
import uk.gov.justice.digital.delius.data.api.deliusapi.NsiDto;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.transformers.NsiPatchRequestTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReferralServiceTest {
    private static final Long OFFENDER_ID = 123L;
    private static final String OFFENDER_CRN = "X123456";
    private static final Long SENTENCE_ID = 2500295343L;
    private static final Long REQUIREMENT_ID = 2500083652L;
    private static final UUID REFERRAL_ID = UUID.randomUUID();
    private static final String REFERRAL_URN = "urn:hmpps:interventions-referral:" + REFERRAL_ID;
    private static final String CONTRACT_TYPE = "ACC";
    private static final String NSI_TYPE = "CR01";
    private static final String PROVIDER_CODE = "CRS";
    private static final String STAFF_CODE = "CRSUATU";
    private static final String TEAM_CODE = "CRSUAT";
    private static final String NSI_STATUS = "INPROG";
    private static final String RAR_TYPE_CODE = "F";
    private static final String CANCELLED_OUTCOME = "CANCELLED";
    private static final String RAR_CONTRACT_TYPE = "CRSAPT";
    private static final String NON_RAR_CONTRACT_TYPE = "CRSSAA";
    private static final Map <String, String> CONTRACT_TYPE_TO_NSI_TYPE_MAPPING = new HashMap<>(){{
        this.put(CONTRACT_TYPE, NSI_TYPE);
    }};
    private static final String INTEGRATION_CONTEXT = "commissioned-rehabilitation-services";

    private static final Nsi MATCHING_NSI = Nsi.builder()
        .nsiId(12345L)
        .nsiType(KeyValue.builder().code(NSI_TYPE).build())
        .referralDate(LocalDate.of(2021, 1, 20))
        .nsiStatus(KeyValue.builder().code(NSI_STATUS).build())
        .requirement(Requirement.builder().requirementId(REQUIREMENT_ID).build())
        .softDeleted(false)
        .intendedProvider(ProbationArea.builder().code(PROVIDER_CODE).build())
        .nsiManagers(singletonList(
            NsiManager.builder()
                .staff(StaffDetails.builder().staffCode(STAFF_CODE).build())
                .team(Team.builder().code(TEAM_CODE).build())
                .probationArea(ProbationArea.builder().code(PROVIDER_CODE).build())
                .build()))
        .externalReference(REFERRAL_URN)
        .notes(REFERRAL_URN + "\nA test note")
        .build();

    private static final ContextlessReferralStartRequest REFERRAL_START_REQUEST = ContextlessReferralStartRequest
        .builder()
        .contractType(CONTRACT_TYPE)
        .startedAt(OffsetDateTime.of(2021, 1, 20, 12, 0, 0, 0, ZoneOffset.UTC))
        .sentenceId(SENTENCE_ID)
        .referralId(REFERRAL_ID)
        .notes("A test note")
        .build();

    private static final ContextlessReferralEndRequest REFERRAL_END_REQUEST = ContextlessReferralEndRequest
        .builder()
        .contractType(CONTRACT_TYPE)
        .startedAt(OffsetDateTime.of(2021, 1, 20, 12, 0, 0, 0, ZoneOffset.UTC))
        .endedAt(OffsetDateTime.of(2021, 1, 21, 12, 0, 0, 0, ZoneOffset.UTC))
        .sentenceId(SENTENCE_ID)
        .referralId(REFERRAL_ID)
        .endType(CANCELLED_OUTCOME)
        .notes("A test note")
        .build();

    @Mock
    OffenderService offenderService;

    @Mock
    DeliusApiClient deliusApiClient;

    @Mock
    NsiService nsiService;

    @Mock
    RequirementService requirementService;

    @Mock
    ContactRepository contactRepository;

    @Mock
    NsiPatchRequestTransformer nsiPatchRequestTransformer;

    @Mock
    TelemetryClient telemetryClient;

    IntegrationContext integrationContext;

    ReferralService referralService;

    @BeforeEach
    public void setup() {
        DeliusIntegrationContextConfig integrationContextConfig = new DeliusIntegrationContextConfig();
        integrationContext = new IntegrationContext();
        integrationContextConfig.getIntegrationContexts().put(INTEGRATION_CONTEXT, integrationContext);
        integrationContext.setProviderCode(PROVIDER_CODE);
        integrationContext.setStaffCode(STAFF_CODE);
        integrationContext.setTeamCode(TEAM_CODE);
        integrationContext.setRequirementRehabilitationActivityType(RAR_TYPE_CODE);
        integrationContext.getNsiMapping().setNsiStatus(NSI_STATUS);
        integrationContext.getNsiMapping().setContractTypeToNsiType(CONTRACT_TYPE_TO_NSI_TYPE_MAPPING);
        integrationContext.getContactMapping().setAppointmentRarContactType(RAR_CONTRACT_TYPE);
        integrationContext.getContactMapping().setAppointmentNonRarContactType(NON_RAR_CONTRACT_TYPE);

        referralService = new ReferralService(telemetryClient, deliusApiClient, nsiService, offenderService, requirementService, nsiPatchRequestTransformer, contactRepository, integrationContextConfig);
    }

    @Nested
    class StartReferrals {

        @Test
        public void creatingNewNsiCallsDeliusApi() {

            Requirement requirement = Requirement.builder().requirementId(REQUIREMENT_ID).build();
            when(requirementService.getActiveRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(of(requirement));
            var deliusApiResponse = NsiDto.builder().id(66853L).build();

            when(deliusApiClient.createNewNsi(any())).thenReturn(deliusApiResponse);

            var response = referralService.startNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_START_REQUEST);

            verify(deliusApiClient).createNewNsi(eq(NewNsi.builder()
                .type(NSI_TYPE)
                .offenderCrn(OFFENDER_CRN)
                .eventId(SENTENCE_ID)
                .requirementId(REQUIREMENT_ID)
                .referralDate(LocalDate.of(2021, 1, 20))
                .expectedStartDate(null)
                .expectedEndDate(null)
                .startDate(LocalDate.of(2021, 1, 20))
                .endDate(null)
                .length(null)
                .status(NSI_STATUS)
                .statusDate(LocalDateTime.of(2021, 1, 20, 12, 0))
                .outcome(null)
                .externalReference(REFERRAL_URN)
                .notes(REFERRAL_URN + "\nA test note")
                .intendedProvider(PROVIDER_CODE)
                .manager(NewNsiManager.builder().staff(STAFF_CODE).team(TEAM_CODE).provider(PROVIDER_CODE).build())
                .build()));

            assertThat(response.getNsiId()).isEqualTo(deliusApiResponse.getId());
        }

        @Test
        public void creatingNewNsiFromReferralRequestWithNoId() {

            Requirement requirement = Requirement.builder().requirementId(REQUIREMENT_ID).build();
            when(requirementService.getActiveRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(of(requirement));
            var deliusApiResponse = NsiDto.builder().id(66853L).build();

            when(deliusApiClient.createNewNsi(any())).thenReturn(deliusApiResponse);

            var response = referralService.startNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_START_REQUEST.withReferralId(null));

            verify(deliusApiClient).createNewNsi(eq(NewNsi.builder()
                .type(NSI_TYPE)
                .offenderCrn(OFFENDER_CRN)
                .eventId(SENTENCE_ID)
                .requirementId(REQUIREMENT_ID)
                .referralDate(LocalDate.of(2021, 1, 20))
                .expectedStartDate(null)
                .expectedEndDate(null)
                .startDate(LocalDate.of(2021, 1, 20))
                .endDate(null)
                .length(null)
                .status(NSI_STATUS)
                .statusDate(LocalDateTime.of(2021, 1, 20, 12, 0))
                .outcome(null)
                .externalReference(null)
                .notes("A test note")
                .intendedProvider(PROVIDER_CODE)
                .manager(NewNsiManager.builder().staff(STAFF_CODE).team(TEAM_CODE).provider(PROVIDER_CODE).build())
                .build()));

            assertThat(response.getNsiId()).isEqualTo(deliusApiResponse.getId());
        }

        @Test
        public void creatingNewNsiCallsDeliusApi_withNoRequirement() {

            when(requirementService.getActiveRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(empty());
            var deliusApiResponse = NsiDto.builder().id(66853L).build();

            when(deliusApiClient.createNewNsi(any())).thenReturn(deliusApiResponse);

            var response = referralService.startNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_START_REQUEST);

            verify(deliusApiClient).createNewNsi(eq(NewNsi.builder()
                .type(NSI_TYPE)
                .offenderCrn(OFFENDER_CRN)
                .eventId(SENTENCE_ID)
                .requirementId(null)
                .referralDate(LocalDate.of(2021, 1, 20))
                .expectedStartDate(null)
                .expectedEndDate(null)
                .startDate(LocalDate.of(2021, 1, 20))
                .endDate(null)
                .length(null)
                .status(NSI_STATUS)
                .statusDate(LocalDateTime.of(2021, 1, 20, 12, 0))
                .outcome(null)
                .externalReference(REFERRAL_URN)
                .notes(REFERRAL_URN + "\nA test note")
                .intendedProvider(PROVIDER_CODE)
                .manager(NewNsiManager.builder().staff(STAFF_CODE).team(TEAM_CODE).provider(PROVIDER_CODE).build())
                .build()));

            assertThat(response.getNsiId()).isEqualTo(deliusApiResponse.getId());
        }
    }

    @Nested
    class EndReferrals {

        @BeforeEach
        public void setUp() {
            when(offenderService.offenderIdOfCrn("X123456")).thenReturn(ofNullable(OFFENDER_ID));
            when(nsiService.getNsiByCodes(OFFENDER_ID, SENTENCE_ID, singletonList(NSI_TYPE)))
                .thenReturn(of(NsiWrapper.builder().nsis(singletonList(MATCHING_NSI)).build()));
        }

        @Test
        public void updatesOutcomeOnNsi() {

            // Given
            when(contactRepository.findByOffenderAndNsiId(OFFENDER_ID, MATCHING_NSI.getNsiId())).thenReturn(emptyList());
            JsonPatch jsonPatch = new JsonPatch(emptyList());
            when(nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(REFERRAL_END_REQUEST, integrationContext)).thenReturn(jsonPatch);

            // When
            var response = referralService.endNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_END_REQUEST);

            // Then
            verify(deliusApiClient).patchNsi(MATCHING_NSI.getNsiId(), jsonPatch);
            assertThat(response.getNsiId()).isEqualTo(MATCHING_NSI.getNsiId());
        }

        @Test
        public void updatesOutcomeOnNsiUsingReferralRequestWithoutId() {

            // Given
            when(contactRepository.findByOffenderAndNsiId(OFFENDER_ID, MATCHING_NSI.getNsiId())).thenReturn(emptyList());
            JsonPatch jsonPatch = new JsonPatch(emptyList());
            ContextlessReferralEndRequest referralEndRequestWithNoId = REFERRAL_END_REQUEST;
            when(nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(referralEndRequestWithNoId, integrationContext)).thenReturn(jsonPatch);

            // When
            var response = referralService.endNsiReferral("X123456", INTEGRATION_CONTEXT, referralEndRequestWithNoId);

            // Then
            verify(deliusApiClient).patchNsi(MATCHING_NSI.getNsiId(), jsonPatch);
            assertThat(response.getNsiId()).isEqualTo(MATCHING_NSI.getNsiId());
        }

        @Test
        public void deletesFutureAppointments() {

            // Given
            when(contactRepository.findByOffenderAndNsiId(OFFENDER_ID, MATCHING_NSI.getNsiId())).thenReturn(
                singletonList(Contact.builder()
                    .contactId(999L)
                    .contactDate(LocalDate.now().minusDays(0))
                    .contactType(ContactType.builder().code("CRSAPT").build())
                    .build()));
            JsonPatch jsonPatch = new JsonPatch(emptyList());
            when(nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(REFERRAL_END_REQUEST, integrationContext)).thenReturn(jsonPatch);

            // When
            referralService.endNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_END_REQUEST);

            // Then
            verify(deliusApiClient).deleteContact(999L);
            verify(deliusApiClient).patchNsi(MATCHING_NSI.getNsiId(), jsonPatch);
        }

        @Test
        public void filtersContactsWithAnExistingOutcomeType() {

            // Given
            when(contactRepository.findByOffenderAndNsiId(OFFENDER_ID, MATCHING_NSI.getNsiId())).thenReturn(
                singletonList(Contact.builder().contactOutcomeType(ContactOutcomeType.builder().build()).build()));
            when(nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(REFERRAL_END_REQUEST, integrationContext))
                .thenReturn(new JsonPatch(emptyList()));

            // When
            referralService.endNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_END_REQUEST);

            // Then
            verify(deliusApiClient, times(0)).deleteContact(any());
        }

        @Test
        public void filtersAppointmentWithOtherContactTypes() {

            // Given
            when(contactRepository.findByOffenderAndNsiId(OFFENDER_ID, MATCHING_NSI.getNsiId())).thenReturn(
                singletonList(Contact.builder().contactType(ContactType.builder().code("ANO").build()).build()));
            when(nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(REFERRAL_END_REQUEST, integrationContext))
                .thenReturn(new JsonPatch(emptyList()));

            // When
            referralService.endNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_END_REQUEST);

            // Then
            verify(deliusApiClient, times(0)).deleteContact(any());
        }

        @Test
        public void filtersHistoricAppointments() {

            // Given
            when(contactRepository.findByOffenderAndNsiId(OFFENDER_ID, MATCHING_NSI.getNsiId())).thenReturn(
                singletonList(Contact.builder()
                    .contactDate(LocalDate.now().minusDays(0))
                    .contactType(ContactType.builder().code("ANO").build())
                    .build()));
            when(nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(REFERRAL_END_REQUEST, integrationContext))
                .thenReturn(new JsonPatch(emptyList()));

            // When
            referralService.endNsiReferral("X123456", INTEGRATION_CONTEXT, REFERRAL_END_REQUEST);

            // Then
            verify(deliusApiClient, times(0)).deleteContact(any());
        }
    }

    @Nested
    class EndReferralsExceptions {

        @Test
        public void throwsExceptionForUnknownNsi() {

            when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(ofNullable(OFFENDER_ID));

            assertThrows(BadRequestException.class,
                () -> referralService.endNsiReferral(OFFENDER_CRN, INTEGRATION_CONTEXT, REFERRAL_END_REQUEST));
        }

        @Test
        public void throwsExceptionForUnknownCrn() {

            when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(ofNullable(OFFENDER_ID));
            when(nsiService.getNsiByCodes(OFFENDER_ID, SENTENCE_ID, singletonList(NSI_TYPE))).thenReturn(empty());

            assertThrows(BadRequestException.class,
                () -> referralService.endNsiReferral(OFFENDER_CRN, INTEGRATION_CONTEXT, REFERRAL_END_REQUEST));
        }
    }

    @Nested
    class GetExistingMatchingNsi {
        @Test
        public void finds_NSI_with_matching_URN_external_reference() {
            when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(of(OFFENDER_ID));
            when(nsiService.getNsisInAnyStateByExternalReferenceURN(eq(OFFENDER_ID), eq(REFERRAL_URN)))
                .thenReturn(List.of(MATCHING_NSI));

            var response = callExistingMatchingNsi(REFERRAL_START_REQUEST);
            assertThat(response.isPresent()).isTrue();
        }

        @Test
        public void throws_BadRequestException_if_multiple_NSIs_found_with_same_URN() {
            when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(of(OFFENDER_ID));
            when(nsiService.getNsisInAnyStateByExternalReferenceURN(eq(OFFENDER_ID), eq(REFERRAL_URN)))
                .thenReturn(List.of(MATCHING_NSI, MATCHING_NSI.withNsiId(23456L)));

            final var exception = assertThrows(BadRequestException.class,
                () -> callExistingMatchingNsi(REFERRAL_START_REQUEST));
            assertThat(exception.getMessage())
                .startsWith("Multiple existing URN NSIs found for referral: " + REFERRAL_START_REQUEST.getReferralId());
        }

        @ParameterizedTest
        @MethodSource("nsiExamples")
        public void finds_NSI_by_fuzzy_matching_if_NSI_with_URN_cannot_be_found(final ContextlessReferralStartRequest nsiRequest, final Nsi existingNsi, final boolean exists) {
            when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(of(OFFENDER_ID));
            when(nsiService.getNsisInAnyStateByExternalReferenceURN(eq(OFFENDER_ID), any()))
                .thenReturn(List.of());
            when(nsiService.getNsiByCodes(eq(OFFENDER_ID), eq(nsiRequest.getSentenceId()), any()))
                .thenReturn(of(NsiWrapper.builder().nsis(singletonList(existingNsi)).build()));

            var response = callExistingMatchingNsi(nsiRequest);
            assertThat(response.isPresent()).isEqualTo(exists);
        }

        @Test
        public void emits_custom_event_for_usage_tracking_when_NSI_with_URN_cannot_be_found() {
            when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(of(OFFENDER_ID));
            when(nsiService.getNsisInAnyStateByExternalReferenceURN(eq(OFFENDER_ID), eq(REFERRAL_URN)))
                .thenReturn(List.of());
            when(nsiService.getNsiByCodes(eq(OFFENDER_ID), eq(REFERRAL_START_REQUEST.getSentenceId()), any()))
                .thenReturn(of(NsiWrapper.builder().nsis(List.of(MATCHING_NSI)).build()));

            callExistingMatchingNsi(REFERRAL_START_REQUEST);
            verify(telemetryClient).trackEvent(
                "community_api.get_existing_matching_nsi.fuzzy_match_fallback",
                Map.of(
                    "crn", "X123456",
                    "contextName", "commissioned-rehabilitation-services",
                    "startedAt", "2021-01-20T12:00Z",
                    "referralId", REFERRAL_ID.toString()
                ),
                null
            );
        }

        @Test
        public void throws_BadRequestException_if_multiple_NSIs_are_found_with_the_fuzzy_lookup() {
            when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(of(OFFENDER_ID));
            when(nsiService.getNsisInAnyStateByExternalReferenceURN(OFFENDER_ID, REFERRAL_URN))
                .thenReturn(List.of());
            when(nsiService.getNsiByCodes(eq(OFFENDER_ID), eq(REFERRAL_START_REQUEST.getSentenceId()), any()))
                .thenReturn(of(NsiWrapper.builder().nsis(List.of(MATCHING_NSI, MATCHING_NSI.withNsiId(23456L))).build()));

            final var exception = assertThrows(BadRequestException.class,
                () -> callExistingMatchingNsi(REFERRAL_START_REQUEST));
            assertThat(exception.getMessage())
                .startsWith("Multiple existing matching NSIs found for referral: " + REFERRAL_START_REQUEST.getReferralId());
        }

        private static Stream<Arguments> nsiExamples() {
            return Stream.of(
                Arguments.of(REFERRAL_START_REQUEST, MATCHING_NSI, true),
                Arguments.of(REFERRAL_START_REQUEST, MATCHING_NSI.withNsiOutcome(KeyValue.builder().code("CANCELLED").build()), false),
                Arguments.of(REFERRAL_START_REQUEST, MATCHING_NSI.withSoftDeleted(false), true),
                Arguments.of(REFERRAL_START_REQUEST, MATCHING_NSI.withSoftDeleted(true), false),
                Arguments.of(REFERRAL_START_REQUEST.withStartedAt(OffsetDateTime.of(2017, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), MATCHING_NSI, false)
            );
        }
    }

    @Test
    public void throwsExceptionIfNsiTypeMappingNotFound() {

        Requirement requirement = Requirement.builder().requirementId(REQUIREMENT_ID).build();
        when(requirementService.getActiveRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(of(requirement));

        var nsiRequest = REFERRAL_START_REQUEST
            .toBuilder()
            .contractType("Unknown")
            .build();

        assertThrows(BadRequestException.class, () -> referralService.startNsiReferral(OFFENDER_CRN, INTEGRATION_CONTEXT, nsiRequest));
    }

    private Optional<Nsi> callExistingMatchingNsi(ContextlessReferralStartRequest referralStartRequest) {
        return referralService.getExistingMatchingNsi(
            OFFENDER_CRN, INTEGRATION_CONTEXT,
            referralStartRequest.getSentenceId(),
            referralStartRequest.getContractType(),
            referralStartRequest.getStartedAt(),
            referralStartRequest.getReferralId()
        );
    }
}
