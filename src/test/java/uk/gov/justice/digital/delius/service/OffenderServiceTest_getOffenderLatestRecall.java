
package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.controller.CustodyNotFoundException;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRelease;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Release;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class OffenderServiceTest_getOffenderLatestRecall {

    private static final Long ANY_OFFENDER_ID = 123L;
    private static final Long SOME_OFFENDER_ID = 456L;
    private Event mockCustodialEvent = mock(Event.class);
    private Disposal mockDisposal = mock(Disposal.class);
    private Custody mockCustody = mock(Custody.class);
    private static final OffenderRelease SOME_OFFENDER_RELEASE = OffenderRelease.builder().build();
    private static final OffenderRecall SOME_OFFENDER_RECALL = OffenderRecall.builder().build();
    private static final OffenderLatestRecall SOME_OFFENDER_LATEST_RECALL =
            OffenderLatestRecall.builder()
                    .lastRelease(SOME_OFFENDER_RELEASE)
                    .lastRecall(SOME_OFFENDER_RECALL)
                    .build();
    private static final OffenderLatestRecall OFFENDER_LATEST_RECALL_NULL_RECALL =
            OffenderLatestRecall.builder()
                    .lastRelease(SOME_OFFENDER_RELEASE)
                    .build();

    @Mock
    private OffenderRepository mockOffenderRepository;
    @Mock
    private ConvictionService mockConvictionService;
    @Mock
    private ReleaseTransformer mockReleaseTransformer;
    @Mock
    private Release mockRelease;

    private OffenderService offenderService;

    @Before
    public void setup() {
        offenderService = new OffenderService(
                mockOffenderRepository,
                new OffenderTransformer(
                        new ContactTransformer()),
                new OffenderManagerTransformer(
                        new StaffTransformer(new TeamTransformer()),
                        new TeamTransformer(),
                        new ProbationAreaTransformer(new InstitutionTransformer())
                ),
                mockConvictionService,
                mockReleaseTransformer
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

        offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        then(mockReleaseTransformer).should().offenderLatestRecallOf(mockRelease);
    }

    @Test
    public void getOffenderLatestRecall_releaseTransformed_returnsReleaseAndRecallFromTransformer() {
        mockHappyPath();

        final var actualOffenderLatestRecall = offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        assertThat(actualOffenderLatestRecall.getLastRelease()).isEqualTo(SOME_OFFENDER_RELEASE);
        assertThat(actualOffenderLatestRecall.getLastRecall()).isEqualTo(SOME_OFFENDER_RECALL);
    }

    private void mockHappyPath() {
        given(mockConvictionService.getActiveCustodialEvent(OffenderServiceTest_getOffenderLatestRecall.SOME_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(mockCustody);
        given(mockCustody.findLatestRelease()).willReturn(Optional.of(mockRelease));
        given(mockReleaseTransformer.offenderLatestRecallOf(mockRelease)).willReturn(SOME_OFFENDER_LATEST_RECALL);
    }

    @Test(expected = ConvictionService.SingleActiveCustodyConvictionNotFoundException.class)
    public void getOffenderLatestRecall_withNoCustodyRecord_propagatesNoCustodyException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willThrow(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);

        offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);
    }

    @Test(expected = CustodyNotFoundException.class)
    public void getOffenderLatestRecall_custodialEventNoDisposal_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(null);

        offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);
    }

    @Test(expected = CustodyNotFoundException.class)
    public void getOffenderLatestRecall_disposalSoftDeleted_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.isSoftDeleted()).willReturn(true);

        offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);
    }

    @Test(expected = CustodyNotFoundException.class)
    public void getOffenderLatestRecall_disposalNoCustody_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(null);

        offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);
    }

    @Test(expected = CustodyNotFoundException.class)
    public void getOffenderLatestRecall_custodySoftDeleted_throwsException() {
        given(mockConvictionService.getActiveCustodialEvent(ANY_OFFENDER_ID)).willReturn(mockCustodialEvent);
        given(mockCustodialEvent.getDisposal()).willReturn(mockDisposal);
        given(mockDisposal.getCustody()).willReturn(mockCustody);
        given(mockCustody.isSoftDeleted()).willReturn(true);

        offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);
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
        given(mockCustody.findLatestRelease()).willReturn(Optional.of(mockRelease));
        given(mockReleaseTransformer.offenderLatestRecallOf(any(Release.class))).willReturn(OFFENDER_LATEST_RECALL_NULL_RECALL);

        final var actualOffenderLatestRecall = offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);

        assertThat(actualOffenderLatestRecall.getLastRelease()).isNotNull();
        assertThat(actualOffenderLatestRecall.getLastRecall()).isNull();
    }


}
