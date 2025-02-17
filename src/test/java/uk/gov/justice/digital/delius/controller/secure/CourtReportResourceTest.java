package uk.gov.justice.digital.delius.controller.secure;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.UserAccessService;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtReportResourceTest {

    private static final String CRN = "X321741";
    private static final Long OFFENDER_ID = 100L;
    private static final Long CONVICTION_ID = 200L;
    private static final CourtReportMinimal courtReportMinimal = CourtReportMinimal.builder().offenderId(OFFENDER_ID).build();

    @Mock
    private ConvictionService convictionService;
    @Mock
    private OffenderService offenderService;
    @Mock
    private CourtReportService courtReportService;
    @Mock
    private UserAccessService userAccessService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CourtReportResource courtReportResource;

    @BeforeEach
    void beforeEach() {
        doReturn(emptyList()).when(authentication).getAuthorities();
    }

    @Test
    void givenKnownCrnAndConvictionId_whenGetReports_thenCheckExclusionsAndReturn() {

        when(offenderService.offenderIdOfCrn(CRN)).thenReturn(Optional.ofNullable(OFFENDER_ID));
        when(convictionService.eventFor(OFFENDER_ID, CONVICTION_ID)).thenReturn(Optional.of(Event.builder().eventId(CONVICTION_ID).build()));
        when(courtReportService.courtReportsMinimalFor(OFFENDER_ID, CONVICTION_ID)).thenReturn(List.of(courtReportMinimal));

        final var courtReport = courtReportResource.getOffenderCourtReportsByCrnAndConvictionId(CRN, CONVICTION_ID, authentication);

        assertThat(courtReport.get(0)).isSameAs(courtReportMinimal);

        verify(userAccessService).checkExclusionsAndRestrictions(eq(CRN), eq(emptyList()));
    }
}
