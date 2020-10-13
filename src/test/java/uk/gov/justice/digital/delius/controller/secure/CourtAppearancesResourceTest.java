package uk.gov.justice.digital.delius.controller.secure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasic;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtAppearancesResourceTest {

    private static final String CRN = "X320741";
    private static final Long OFFENDER_ID = 99L;
    private static final Long CONVICTION_ID = 2500295345L;

    @Mock
    private CourtAppearanceService courtAppearanceService;

    @Mock
    private OffenderService offenderService;

    @InjectMocks
    private CourtAppearancesResource courtAppearancesResource;

    @Test
    void whenGetAppearances_thenRespondWithWrapper() {

        var courtAppearance = CourtAppearanceBasic.builder()
            .appearanceDate(LocalDateTime.now())
            .courtCode("SHF")
            .build();

        when(offenderService.offenderIdOfCrn(CRN)).thenReturn(Optional.of(OFFENDER_ID));
        when(courtAppearanceService.courtAppearancesFor(OFFENDER_ID, CONVICTION_ID)).thenReturn(List.of(courtAppearance));

        var courtAppearancesWrapper = courtAppearancesResource.getOffenderCourtAppearancesByCrn(CRN, CONVICTION_ID);

        assertThat(courtAppearancesWrapper.getCourtAppearances()).hasSize(1);
        assertThat(courtAppearancesWrapper.getCourtAppearances().get(0)).isSameAs(courtAppearance);
        verify(offenderService).offenderIdOfCrn(CRN);
        verify(courtAppearanceService).courtAppearancesFor(OFFENDER_ID, CONVICTION_ID);
        verifyNoMoreInteractions(offenderService, courtAppearanceService);
    }

    @Test
    void givenCrnNotFound_whenGetAppearances_thenThrowException() {

        when(offenderService.offenderIdOfCrn("XXX")).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> courtAppearancesResource.getOffenderCourtAppearancesByCrn("XXX", CONVICTION_ID));

        verify(offenderService).offenderIdOfCrn("XXX");
        verifyNoMoreInteractions(offenderService, courtAppearanceService);
    }

}
