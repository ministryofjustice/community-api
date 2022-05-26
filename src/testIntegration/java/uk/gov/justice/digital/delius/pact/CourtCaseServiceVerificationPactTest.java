package uk.gov.justice.digital.delius.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.controller.secure.IntegrationTestBase;
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.data.api.ProbationStatus;
import uk.gov.justice.digital.delius.data.api.ProbationStatusDetail;
import uk.gov.justice.digital.delius.data.api.ReportManager;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Provider("community-api")
@PactBroker(url = "https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk",
    consumerVersionSelectors = {
        @VersionSelector(tag = "main", latest = "false")
    })
//@Disabled
class CourtCaseServiceVerificationPactTest extends IntegrationTestBase {

    public static final String CRN = "X320741";
    public static final Long OFFENDER_ID = 99L;
    public static final Long CONVICTION_ID = 2500295345L;

    @MockBean
    OffenderService offenderService;

    @MockBean
    NsiService nsiService;

    @MockBean
    ConvictionService convictionService;

    @MockBean
    CourtReportService courtReportService;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactTestTemplate(PactVerificationContext context, HttpRequest request) {
        request.addHeader("Authorization", "Bearer " + tokenWithRoleCommunity());
        context.verifyInteraction();
    }

    @State({
        "an NSI exists for CRN X320741 and conviction id 2500295345"
    })
    void nsiExistsForCrnAndConvictionId() {
        Mockito.when(offenderService.offenderIdOfCrn(CRN)).thenReturn(Optional.of(OFFENDER_ID));

        Collection<String> list = new ArrayList<>();
        list.add("BRE");
        Mockito.when(nsiService.getNsiByCodes(OFFENDER_ID, CONVICTION_ID, list))
            .thenReturn(Optional.of(
                NsiWrapper.builder()
                    .nsis(List.of(
                        Nsi.builder()
                            .actualStartDate(LocalDate.now())
                            .nsiId(1747823658L)
                            .nsiStatus(KeyValue.builder().code("description").build())
                            .nsiSubType(KeyValue.builder().code("description").build())
                            .nsiType(KeyValue.builder().code("description").build())
                            .statusDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                            .build()
                    )).build()
            ));
    }

    @State({
        "an offender exists with CRN X320741 with conviction ID 2500295345"
    })
    void aRequestForCourtReportsForCrnAndConvictionId() {
        final LocalDateTime datetime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final CourtReportMinimal crm = CourtReportMinimal.builder()
            .completedDate(datetime)
            .courtReportId(56436372L)
            .courtReportType(KeyValue.builder().code("code").description("Description").build())
            .offenderId(OFFENDER_ID)
            .reportManagers(List.of(ReportManager.builder()
                .active(true)
                .staff(StaffHuman.builder()
                    .code("StaffCode")
                    .forenames("Fred")
                    .surname("Flintstone")
                    .build())
                .build()))
            .requestedDate(datetime)
            .requiredDate(datetime)
            .build();

        Mockito.when(offenderService.offenderIdOfCrn(CRN)).thenReturn(Optional.of(OFFENDER_ID));
        Mockito.when(convictionService.eventFor(OFFENDER_ID, CONVICTION_ID)).thenReturn(Optional.of(Event.builder().build()));
        Mockito.when(courtReportService.courtReportsMinimalFor(OFFENDER_ID, CONVICTION_ID)).thenReturn(List.of(crm));
    }

    @State({
        "an offender exists with CRN X320741"
    })
    void aRequestForProbationStatus() {
        Mockito.when(convictionService.probationStatusFor(CRN)).thenReturn(Optional.of(
            ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .inBreach(true)
                .status(ProbationStatus.CURRENT)
                .preSentenceActivity(true)
                .previouslyKnownTerminationDate(LocalDate.now())
                .build()
        ));
    }

    @State({
        "probation status detail is available for CRN X320741"
    })
    void aRequestForCurrentProbationStatus() {
        Mockito.when(convictionService.probationStatusFor(CRN)).thenReturn(Optional.of(
            ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .inBreach(true)
                .status(ProbationStatus.CURRENT)
                .preSentenceActivity(true)
                .build()
        ));
    }

    @State({
        "probation status detail is available for CRN CRN40"
    })
    void aRequestForPreviouslyKnownProbationStatus() {
        Mockito.when(convictionService.probationStatusFor("CRN40")).thenReturn(Optional.of(
            ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .status(ProbationStatus.CURRENT)
                .preSentenceActivity(true)
                .previouslyKnownTerminationDate(LocalDate.now())
                .build()
        ));
    }
}
