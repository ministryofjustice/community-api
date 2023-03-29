
package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.CustodyNotFoundException;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderAccessLimitationRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class OffenderServiceTest_getOffenderLatestRecall {

    private static final Long ANY_OFFENDER_ID = 123L;
    private static final Long SOME_OFFENDER_ID = 456L;
    private Event mockCustodialEvent = mock(Event.class);
    private Disposal mockDisposal = mock(Disposal.class);
    private Custody mockCustody = mock(Custody.class);

    @Mock
    private OffenderRepository mockOffenderRepository;

    @Mock
    private OffenderAccessLimitationRepository mockOffenderAccessLimitationRepository;

    @Mock
    private ConvictionService mockConvictionService;

    @Mock
    private OffenderDocumentRepository mockOffenderDocumentRepository;

    private OffenderService offenderService;

    @BeforeEach
    public void setup() {
        offenderService = new OffenderService(
                mockOffenderRepository,
                mockOffenderAccessLimitationRepository,
                mockConvictionService,
                mockOffenderDocumentRepository
        );
    }

    @Test
    public void getOffenderLatestRecall_offenderFound_retrievesActiveCustodialEvent() {
        mockHappyPath();

        offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        then(mockConvictionService).should().getActiveCustodialEvent(SOME_OFFENDER_ID);
    }

    @Test
    public void getOffenderLatestRecall_disposalFound_retrievesCustody() {
        mockHappyPath();

        offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        then(mockDisposal).should().getCustody();
    }

    @Test
    public void getOffenderLatestRecall_custodyFound_retrievesRelease() {
        mockHappyPath();

        offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        then(mockCustody).should().findLatestRelease();
    }

    @Test
    public void getOffenderLatestRecall_releaseFound_transformsRelease() {
        mockHappyPath();

        final var release = offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        assertThat(release.getLastRelease()).isNotNull();
    }

    @Test
    public void getOffenderLatestRecall_releaseTransformed_returnsReleaseAndRecallFromTransformer() {
        mockHappyPath();

        given(mockCustody.findLatestRelease()).willReturn(Optional.of(EntityHelper.aRelease().toBuilder()
                .notes("Released for geed behaviour")
                .recalls(List.of(EntityHelper.aRecall().toBuilder().notes("Recalled, bad person").build()))
                .build()));

        final var actualOffenderLatestRecall = offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        assertThat(actualOffenderLatestRecall.getLastRelease().getNotes()).isEqualTo("Released for geed behaviour");
        assertThat(actualOffenderLatestRecall.getLastRecall().getNotes()).isEqualTo("Recalled, bad person");
    }

    private void mockHappyPath() {
        given(mockConvictionService.getActiveCustodialEvent(OffenderServiceTest_getOffenderLatestRecall.SOME_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(mockCustody);
        given(mockCustody.findLatestRelease()).willReturn(Optional.of(Release
                .builder()
                .actualReleaseDate(LocalDateTime.now())
                .institution(EntityHelper.aPrisonInstitution())
                .releaseId(99L)
                .releaseType(StandardReference.builder().build())
                .build()));
    }

    @Test
    public void getOffenderLatestRecall_withNoCustodyRecord_propagatesNoCustodyException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willThrow(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);

        assertThrows(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class,
            () -> { offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID); });
    }

    @Test
    public void getOffenderLatestRecall_custodialEventNoDisposal_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(null);

        assertThrows(CustodyNotFoundException.class,
            () -> { offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID); });
    }

    @Test
    public void getOffenderLatestRecall_disposalSoftDeleted_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.isSoftDeleted()).willReturn(true);

        assertThrows(CustodyNotFoundException.class,
            () -> { offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID); });
    }

    @Test
    public void getOffenderLatestRecall_disposalNoCustody_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(null);

        assertThrows(CustodyNotFoundException.class,
            () -> { offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID); });
    }

    @Test
    public void getOffenderLatestRecall_custodySoftDeleted_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(mockCustody);
        given(mockCustody.isSoftDeleted()).willReturn(true);

        assertThrows(CustodyNotFoundException.class,
            () -> { offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID); });
    }

    @Test
    public void getOffenderLatestRecall_noRelease_returnsNullReleaseAndRecall() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(mockCustody);
        given(mockCustody.findLatestRelease()).willReturn(Optional.empty());

        final var actualOffenderLatestRecall = offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);

        assertThat(actualOffenderLatestRecall).isEqualTo(OffenderLatestRecall.NO_RELEASE);
    }

    @Test
    public void getOffenderLatestRecall_releaseNoRecall_returnsNullRecall() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(mockCustody);
        given(mockCustody.findLatestRelease()).willReturn(Optional.of(EntityHelper.aRelease()));

        final var actualOffenderLatestRecall = offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);

        assertThat(actualOffenderLatestRecall.getLastRelease()).isNotNull();
        assertThat(actualOffenderLatestRecall.getLastRecall()).isNull();
    }


}
