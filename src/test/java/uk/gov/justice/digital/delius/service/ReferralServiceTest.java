package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiManager;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsiManager;
import uk.gov.justice.digital.delius.data.api.deliusapi.NsiDto;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReferralServiceTest {
    private static final String NSI_CODE = "IPT";
    private static final Long OFFENDER_ID = 123L;
    private static final String OFFENDER_CRN = "X123456";
    private static final Long CONVICTION_ID = 2500295343L;
    private static final Long REQUIREMENT_ID = 2500083652L;

    private static final Nsi MATCHING_NSI = Nsi.builder()
        .nsiId(12345L)
        .nsiType(KeyValue.builder().code(NSI_CODE).build())
        .nsiSubType(KeyValue.builder().code("IPT1").build())
        .referralDate(LocalDate.of(2021, 1, 20))
        .nsiStatus(KeyValue.builder().code("REFER").build())
        .requirement(Requirement.builder().requirementId(REQUIREMENT_ID).build())
        .intendedProvider(ProbationArea.builder().code("YSS").build())
        .nsiManagers(singletonList(
            NsiManager.builder()
                .staff(StaffDetails.builder().staffCode("N06AAFU").build())
                .team(Team.builder().code("N05MKU").build())
                .probationArea(ProbationArea.builder().code("YSS").build())
                .build()))
        .build();

    private static final ReferralSentRequest NSI_REQUEST = ReferralSentRequest
        .builder()
        .nsiType(NSI_CODE)
        .nsiSubType("IPT1")
        .date(LocalDate.of(2021, 1, 20))
        .nsiStatus("REFER")
        .requirementId(REQUIREMENT_ID)
        .providerCode("YSS")
        .staffCode("N06AAFU")
        .teamCode("N05MKU")
        .convictionId(CONVICTION_ID)
        .notes("A test note")
        .build();

    @Mock
    OffenderService offenderService;

    @Mock
    DeliusApiClient deliusApiClient;

    @Mock
    NsiService nsiService;

    @InjectMocks
    ReferralService referralService;

    @BeforeEach
    public void setup() {
        when(offenderService.offenderIdOfCrn(OFFENDER_CRN)).thenReturn(Optional.of(OFFENDER_ID));
    }

    @Test
    public void creatingNewNsiWhenMatchingOneExistsReturnsExistingNsi() {
        when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(Optional.of(NsiWrapper.builder().nsis(singletonList(MATCHING_NSI)).build()));

        var response = referralService.createNsiReferral("X123456", NSI_REQUEST);

        verify(nsiService).getNsiByCodes(OFFENDER_ID, CONVICTION_ID, singletonList(NSI_CODE));
        verifyNoInteractions(deliusApiClient);

        assertThat(response.getNsiId()).isEqualTo(MATCHING_NSI.getNsiId());
    }

    @Test
    public void creatingNewNsiCallsDeliusApiWhenNonExisting() {
        var deliusApiResponse = NsiDto.builder().id(66853L).build();

        when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(Optional.of(NsiWrapper.builder().nsis(Collections.emptyList()).build()));
        when(deliusApiClient.createNewNsi(any())).thenReturn(deliusApiResponse);

        var response = referralService.createNsiReferral("X123456", NSI_REQUEST);

        verify(nsiService).getNsiByCodes(OFFENDER_ID, CONVICTION_ID, singletonList(NSI_CODE));

        verify(deliusApiClient).createNewNsi(eq(NewNsi.builder()
            .type(NSI_CODE)
            .subType("IPT1")
            .offenderCrn(OFFENDER_CRN)
            .eventId(CONVICTION_ID)
            .requirementId(REQUIREMENT_ID)
            .referralDate(LocalDate.of(2021, 1, 20))
            .expectedStartDate(null)
            .expectedEndDate(null)
            .startDate(null)
            .endDate(null)
            .length(null)
            .status("REFER")
            .statusDate(LocalDateTime.of(2021, 1, 20, 0, 0))
            .outcome(null)
            .notes("A test note")
            .intendedProvider("YSS")
            .manager(NewNsiManager.builder().staff("N06AAFU").team("N05MKU").provider("YSS").build())
            .build()));

        assertThat(response.getNsiId()).isEqualTo(deliusApiResponse.getId());
    }

    @Nested
    @TestInstance(PER_CLASS)
    class NsiMatching {
        private Stream<Arguments> nsis() {
            return Stream.of(
                Arguments.of(MATCHING_NSI, true),
                Arguments.of(MATCHING_NSI.withNsiSubType(KeyValue.builder().code("NOMATCH").build()), false),
                Arguments.of(MATCHING_NSI.withReferralDate(LocalDate.of(2017, 1, 1)), false),
                Arguments.of(MATCHING_NSI.withNsiStatus(KeyValue.builder().code("NOMATCH").build()), false),
                Arguments.of(MATCHING_NSI.withRequirement(Requirement.builder().requirementId(1L).build()), false),
                Arguments.of(MATCHING_NSI.withIntendedProvider(ProbationArea.builder().code("NOMATCH").build()), false),
                Arguments.of(MATCHING_NSI.withNsiManagers(singletonList(MATCHING_NSI.getNsiManagers().get(0).withStaff(StaffDetails.builder().staffCode("NOMATCH").build()))), false),
                Arguments.of(MATCHING_NSI.withNsiManagers(singletonList(MATCHING_NSI.getNsiManagers().get(0).withTeam(Team.builder().code("NOMATCH").build()))), false),
                Arguments.of(MATCHING_NSI.withNsiManagers(singletonList(MATCHING_NSI.getNsiManagers().get(0).withProbationArea(ProbationArea.builder().code("NOMATCH").build()))), false)
            );
        }

        @ParameterizedTest
        @MethodSource("nsis")
        public void noNsisAreReturnedWhenNotExactMatch(final Nsi nsi, final boolean exists) {
            when(nsiService.getNsiByCodes(any(), any(), any())).thenReturn(Optional.of(NsiWrapper.builder().nsis(singletonList(nsi)).build()));

            var response = referralService.getExistingMatchingNsi(OFFENDER_CRN, NSI_REQUEST);

            assertThat(response.isPresent()).isEqualTo(exists);
        }
    }
}
