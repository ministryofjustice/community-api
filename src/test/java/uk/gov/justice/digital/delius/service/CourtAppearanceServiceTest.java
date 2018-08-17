package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class CourtAppearanceServiceTest  {

/* WIP - Test that a list of JPA Database CourtAppearances with some soft deleted are filtered out
    @Autowired
    private CourtAppearanceService courtAppearanceService;

    @MockBean
    private CourtAppearanceRepository courtAppearanceRepository;

    // CourtAppearanceRepository


    @Test
    public void softDeletedRecordsAreExcluded() {

        when(courtAppearanceRepository.findByOffenderId(1L)).thenReturn(ImmutableList.of(

                new CourtAppearance(1L, LocalDateTime.now(), "", "", "", 1L, 1L, 1L, 1L, )
        ))

        List<CourtAppearance> test = courtAppearanceService.courtAppearancesFor(1L);


        return;
    }
*/
}