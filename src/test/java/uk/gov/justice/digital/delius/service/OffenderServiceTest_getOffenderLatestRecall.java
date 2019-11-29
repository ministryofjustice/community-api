
package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class OffenderServiceTest_getOffenderLatestRecall {

    private static final Long SOME_OFFENDER_ID = 123L;
    private static final Long ANY_OFFENDER_ID = 456L;
    private static final Optional<Offender> ANY_OFFENDER = Optional.of(Offender.builder().offenderId(ANY_OFFENDER_ID).build());

    @Mock
    private OffenderRepository mockOffenderRepository;

    private OffenderService offenderService;

    @Before
    public void setup() {
        offenderService = new OffenderService(
                mockOffenderRepository,
                new OffenderTransformer(
                        new ContactTransformer()),
                new OffenderManagerTransformer(
                        new StaffTransformer(
                                new TeamTransformer()),
                        new TeamTransformer(),
                        new ProbationAreaTransformer(
                                new InstitutionTransformer())));
    }

    @Test
    public void getOffenderLatestRecall_withOffenderId_searchesForOffenderInRepository() {
        given(mockOffenderRepository.findByOffenderId(SOME_OFFENDER_ID))
                .willReturn(Optional.empty());

        offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        then(mockOffenderRepository).should().findByOffenderId(SOME_OFFENDER_ID);
    }

    @Test
    public void getOffenderLatestRecall_offenderNotFound_returnsEmpty() {
        given(mockOffenderRepository.findByOffenderId(SOME_OFFENDER_ID))
                .willReturn(Optional.empty());

        final var actualOffenderRecall = offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        assertThat(actualOffenderRecall.isEmpty()).isTrue();
    }
}
