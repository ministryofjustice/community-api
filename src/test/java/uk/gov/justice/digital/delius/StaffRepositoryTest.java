package uk.gov.justice.digital.delius;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class StaffRepositoryTest {

    @LocalServerPort
    int port;

    @Autowired
    private StaffRepository staffRepository;

    @Test
    public void canSeeTheStaff() {
        Optional<Staff> maybeStaff = staffRepository.findByStaffId(111L);
        System.out.println(maybeStaff);
    }
}