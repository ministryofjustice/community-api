package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
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
            assertThat(maybeStaff.get().getSurname()).isEqualTo("HANCOCK");
        }
    }
}
