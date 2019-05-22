package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

@RunWith(SpringRunner.class)
@Import({OffenderService.class, OffenderTransformer.class})
public class ResponsibleOfficerTest {

    @Autowired
    private OffenderService offenderService;

    @MockBean
    private OffenderRepository offenderRepository;

    @Before
    public void setup() {
        // setup the mocked response data from the OffenderService for the following scenarios
    }

    @Test
    public void responsibleOfficerSingle() {
        // OK - single RO returned
        // Verify record content - fully populated
    }

    @Test
    public void responsibleOfficerMoreThanOne() {
        // OK - one OM and one POM returned
        // Verify # of records returned
        // Verify content of records
        // Verify both current and historical data
    }

    @Test
    public void responsibleOfficerStaffNotNotFound () {
        // 404 exception raised - not found
        // Verify content of message
    }

    @Test
    public void noResponsibleOfficerAllocated() {
        // 200 but empty list
        // Verify list is empty
    }
}
