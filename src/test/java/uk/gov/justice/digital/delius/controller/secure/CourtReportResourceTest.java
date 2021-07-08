package uk.gov.justice.digital.delius.controller.secure;

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
    private static final Long REPORT_ID = 1L;
    private static final Long OFFENDER_ID = 100L;
    private static final CourtReportMinimal courtReportMinimal = CourtReportMinimal.builder().offenderId(OFFENDER_ID).build();

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
    void givenKnownCrnAndReportId_whenGetReports_thenCheckExclusionsAndReturn() {

        when(offenderService.offenderIdOfCrn(CRN)).thenReturn(Optional.of(OFFENDER_ID));
        when(courtReportService.courtReportMinimalFor(OFFENDER_ID, REPORT_ID)).thenReturn(Optional.of(courtReportMinimal));

        final var courtReport = courtReportResource.getOffenderCourtReportByCrnAndCourtReportId(CRN, REPORT_ID, authentication);

        assertThat(courtReport).isSameAs(courtReportMinimal);

        verify(userAccessService).checkExclusionsAndRestrictions(eq(CRN), eq(emptyList()));
    }

    @Test
    void givenUnknownCrn_whenGetReports_thenCheckExclusionsAndReturn404() {

        when(offenderService.offenderIdOfCrn(CRN)).thenReturn(Optional.empty());

        var thrown = assertThrows(
            NotFoundException.class,
            () -> courtReportResource.getOffenderCourtReportByCrnAndCourtReportId(CRN, REPORT_ID, authentication)
        );
        assertThat(thrown.getMessage()).contains("Offender with crn X321741 not found");

        verify(userAccessService).checkExclusionsAndRestrictions(eq(CRN), eq(emptyList()));
    }

    @Test
    void givenUnknownReportId_whenGetReports_thenCheckExclusionsAndReturn404() {

        when(offenderService.offenderIdOfCrn(CRN)).thenReturn(Optional.of(OFFENDER_ID));
        when(courtReportService.courtReportMinimalFor(OFFENDER_ID, REPORT_ID)).thenReturn(Optional.empty());

        var thrown = assertThrows(
            NotFoundException.class,
            () -> courtReportResource.getOffenderCourtReportByCrnAndCourtReportId(CRN, REPORT_ID, authentication)
        );
        assertThat(thrown.getMessage()).contains("Court report with ID 1 not found");

        verify(userAccessService).checkExclusionsAndRestrictions(eq(CRN), eq(emptyList()));
    }
}
