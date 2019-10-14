package uk.gov.justice.digital.delius;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
@Transactional
@DirtiesContext
public class StaffRepositoryTest {

    @LocalServerPort
    int port;

    @Autowired
    private StaffRepository staffRepository;

    @Test
    public void canSeeTheStaff() {
        Optional<Staff> maybeStaff = staffRepository.findByStaffId(11L);
        if (maybeStaff.isPresent()) {
            assertThat(maybeStaff.get().getSurname()).isEqualTo("HANCOCK");
        }
    }
}
