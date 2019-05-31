package uk.gov.justice.digital.delius;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;

import javax.transaction.Transactional;
import java.util.Optional;

/*
 * Uncomment this test and run manually to ensure that the data is loading
 * and a Staff record can be found by ID.
 */

@Ignore
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class StaffRepositoryTest {

    @LocalServerPort
    int port;

    @Autowired
    private StaffRepository staffRepository;

    @Test
    public void canSeeTheStaff() {
        Optional<Staff> maybeStaff = staffRepository.findByStaffId(11L);
        if (maybeStaff.isPresent()) {
            System.out.println("STAFF OBJECT = " + maybeStaff.get().getSurname());
        }
    }
}
