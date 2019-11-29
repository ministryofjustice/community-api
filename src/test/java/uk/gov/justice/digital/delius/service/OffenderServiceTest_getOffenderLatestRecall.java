
package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.*;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class OffenderServiceTest_getOffenderLatestRecall {

    private static final Long ANY_OFFENDER_ID = 123L;
    private static final Long SOME_OFFENDER_ID = 456L;
    private static final Optional<Offender> SOME_OFFENDER = Optional.of(Offender.builder().offenderId(SOME_OFFENDER_ID).build());

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
                .willReturn(SOME_OFFENDER);

        offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID);

        then(mockOffenderRepository).should().findByOffenderId(SOME_OFFENDER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void getOffenderLatestRecall_offenderNotFound_throwsNotFound() {
        given(mockOffenderRepository.findByOffenderId(ANY_OFFENDER_ID))
                .willReturn(Optional.empty());

        offenderService.getOffenderLatestRecall(ANY_OFFENDER_ID);
    }
}
