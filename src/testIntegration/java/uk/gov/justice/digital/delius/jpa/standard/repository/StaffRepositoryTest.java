package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

import jakarta.transaction.Transactional;
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
        maybeStaff.ifPresent(staff -> assertThat(staff.getSurname()).isEqualTo("HANCOCK"));
    }
}
