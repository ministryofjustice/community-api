package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

@RunWith(SpringRunner.class)
@Import({StaffService.class, OffenderTransformer.class})
public class ManagedOffenderTest {

    @Autowired
    private StaffService staffService;

    @MockBean
    private StaffRepository staffRepository;


    @Before
    public void setup() {
        // setup the mocked response data from the StaffRespository for the following scenarios
    }

    @Test
    public void managedOffendersForOfficer() {
        // OK - known officer, get all managed offenders - 3 returned
        // Verify # records returned
        // Verify nomsNumbers in returned offenders
        // Verify surname, forename, DOB, crn for one offender - to ensure populated.
    }

    @Test
    public void managedOffendersWithHistory() {
        // OK - list of offenders + a history with datesFrom/to and active
        // Verify # records returned
        // Verify nomsNumbers of returned offenders.
    }

    @Test
    public void offenderNotFound () {
        // 404 exception raised - not found
        // Verify exception raised.
    }

    @Test
    public void managedOffendersCurrent() {
        // OK - filtered to only the currently managed offenders
        // Use same officer_code as in managedOffendersWithHistory test
        // Verify the # of records returned.
        // Verify nomsNumbers of currently managed offenders
    }
}
