package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReferralServiceTest {
    private static final Long OFFENDER_ID = 123L;
    private static final String OFFENDER_CRN = "X123456";
    private static final Long SENTENCE_ID = 2500295343L;
    private static final Long REQUIREMENT_ID = 2500083652L;
    private static final String CONTRACT_TYPE = "ACC";
    private static final String NSI_TYPE = "CR01";
    private static final String PROVIDER_CODE = "CRS";
    private static final String STAFF_CODE = "CRSUATU";
    private static final String TEAM_CODE = "CRSUAT";
    private static final String NSI_STATUS = "INPROG";
    private static final String RAR_TYPE_CODE = "F";
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
        .intendedProvider(ProbationArea.builder().code(PROVIDER_CODE).build())
        .nsiManagers(singletonList(
            NsiManager.builder()
                .staff(StaffDetails.builder().staffCode(STAFF_CODE).build())
                .team(Team.builder().code(TEAM_CODE).build())
                .probationArea(ProbationArea.builder().code(PROVIDER_CODE).build())
                .build()))
        .build();

    private static final ContextlessReferralStartRequest NSI_REQUEST = ContextlessReferralStartRequest
        .builder()
        .contractType(CONTRACT_TYPE)
        .startedAt(OffsetDateTime.of(2021, 1, 20, 12, 0, 0, 0, ZoneOffset.UTC))
        .sentenceId(SENTENCE_ID)
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

    ReferralService referralService;

    @BeforeEach
    public void setup() {
        DeliusIntegrationContextConfig integrationContextConfig = new DeliusIntegrationContextConfig();
        IntegrationContext integrationContext = new IntegrationContext();
        integrationContextConfig.getIntegrationContexts().put(INTEGRATION_CONTEXT, integrationContext);
        integrationContext.setProviderCode(PROVIDER_CODE);
        integrationContext.setStaffCode(STAFF_CODE);
        integrationContext.setTeamCode(TEAM_CODE);
        integrationContext.setRequirementRehabilitationActivityType(RAR_TYPE_CODE);
        integrationContext.getNsiMapping().setNsiStatus(NSI_STATUS);
        integrationContext.getNsiMapping().setContractTypeToNsiType(CONTRACT_TYPE_TO_NSI_TYPE_MAPPING);

        referralService = new ReferralService(deliusApiClient, nsiService, offenderService, requirementService, integrationContextConfig);

        when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(of(OFFENDER_ID));
    }

    @Test
    public void creatingNewNsiWhenMatchingOneExistsReturnsExistingNsi() {

        Requirement requirement = Requirement.builder().requirementId(REQUIREMENT_ID).build();
        when(requirementService.getRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(of(requirement));
        when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(of(NsiWrapper.builder().nsis(singletonList(MATCHING_NSI)).build()));

        var response = referralService.startNsiReferral("X123456", INTEGRATION_CONTEXT, NSI_REQUEST);

        verify(nsiService).getNsiByCodes(OFFENDER_ID, SENTENCE_ID, singletonList(NSI_TYPE));
        verifyNoInteractions(deliusApiClient);

        assertThat(response.getNsiId()).isEqualTo(MATCHING_NSI.getNsiId());
    }

    @Test
    public void creatingNewNsiWhenMultipleMatchingExistReturnsConflict() {

        Requirement requirement = Requirement.builder().requirementId(REQUIREMENT_ID).build();
        when(requirementService.getRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(of(requirement));
        when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(of(NsiWrapper.builder().nsis(Collections.nCopies(2, MATCHING_NSI)).build()));

        assertThrows(ConflictingRequestException.class, () -> referralService.startNsiReferral("X123456", INTEGRATION_CONTEXT, NSI_REQUEST));

        verify(nsiService).getNsiByCodes(OFFENDER_ID, SENTENCE_ID, singletonList(NSI_TYPE));
        verifyNoInteractions(deliusApiClient);
    }

    @Test
    public void creatingNewNsiCallsDeliusApiWhenNonExisting() {

        Requirement requirement = Requirement.builder().requirementId(REQUIREMENT_ID).build();
        when(requirementService.getRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(of(requirement));
        var deliusApiResponse = NsiDto.builder().id(66853L).build();

        when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(of(NsiWrapper.builder().nsis(Collections.emptyList()).build()));
        when(deliusApiClient.createNewNsi(any())).thenReturn(deliusApiResponse);

        var response = referralService.startNsiReferral("X123456", INTEGRATION_CONTEXT, NSI_REQUEST);

        verify(nsiService).getNsiByCodes(OFFENDER_ID, SENTENCE_ID, singletonList(NSI_TYPE));

        verify(deliusApiClient).createNewNsi(eq(NewNsi.builder()
            .type(NSI_TYPE)
            .offenderCrn(OFFENDER_CRN)
            .eventId(SENTENCE_ID)
            .requirementId(REQUIREMENT_ID)
            .referralDate(LocalDate.of(2021, 1, 20))
            .expectedStartDate(null)
            .expectedEndDate(null)
            .startDate(null)
            .endDate(null)
            .length(null)
            .status(NSI_STATUS)
            .statusDate(LocalDateTime.of(2021, 1, 20, 12, 0))
            .outcome(null)
            .notes("A test note")
            .intendedProvider(PROVIDER_CODE)
            .manager(NewNsiManager.builder().staff(STAFF_CODE).team(TEAM_CODE).provider(PROVIDER_CODE).build())
            .build()));

        assertThat(response.getNsiId()).isEqualTo(deliusApiResponse.getId());
    }

    @Test
    public void creatingNewNsiCallsDeliusApiWhenNonExisting_withNoRequirement() {

        when(requirementService.getRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(empty());
        var deliusApiResponse = NsiDto.builder().id(66853L).build();

        when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(of(NsiWrapper.builder().nsis(Collections.emptyList()).build()));
        when(deliusApiClient.createNewNsi(any())).thenReturn(deliusApiResponse);

        var response = referralService.startNsiReferral("X123456", INTEGRATION_CONTEXT, NSI_REQUEST);

        verify(nsiService).getNsiByCodes(OFFENDER_ID, SENTENCE_ID, singletonList(NSI_TYPE));

        verify(deliusApiClient).createNewNsi(eq(NewNsi.builder()
            .type(NSI_TYPE)
            .offenderCrn(OFFENDER_CRN)
            .eventId(SENTENCE_ID)
            .requirementId(null)
            .referralDate(LocalDate.of(2021, 1, 20))
            .expectedStartDate(null)
            .expectedEndDate(null)
            .startDate(null)
            .endDate(null)
            .length(null)
            .status(NSI_STATUS)
            .statusDate(LocalDateTime.of(2021, 1, 20, 12, 0))
            .outcome(null)
            .notes("A test note")
            .intendedProvider(PROVIDER_CODE)
            .manager(NewNsiManager.builder().staff(STAFF_CODE).team(TEAM_CODE).provider(PROVIDER_CODE).build())
            .build()));

        assertThat(response.getNsiId()).isEqualTo(deliusApiResponse.getId());
    }

    @ParameterizedTest
    @MethodSource("nsis")
    public void noNsisAreReturnedWhenNotExactMatch(final ContextlessReferralStartRequest nsiRequest, final Nsi existingNsi, final boolean exists) {

        when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(of(NsiWrapper.builder().nsis(singletonList(existingNsi)).build()));

        var response = referralService.getExistingMatchingNsi(OFFENDER_CRN, INTEGRATION_CONTEXT, nsiRequest);

        assertThat(response.isPresent()).isEqualTo(exists);
    }

    @Test
    public void throwsExceptionIfNsiTypeMappingNotFound() {

        Requirement requirement = Requirement.builder().requirementId(REQUIREMENT_ID).build();
        when(requirementService.getRequirement(OFFENDER_CRN, SENTENCE_ID, RAR_TYPE_CODE)).thenReturn(of(requirement));

        var nsiRequest = NSI_REQUEST
            .toBuilder()
            .contractType("Unknown")
            .build();

        assertThrows(IllegalArgumentException.class, () -> referralService.startNsiReferral(OFFENDER_CRN, INTEGRATION_CONTEXT, nsiRequest));
    }

    private static Stream<Arguments> nsis() {
        return Stream.of(
            Arguments.of(NSI_REQUEST, MATCHING_NSI, true),
            Arguments.of(NSI_REQUEST.withStartedAt(OffsetDateTime.of(2017, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), MATCHING_NSI, false)
        );
    }
}
