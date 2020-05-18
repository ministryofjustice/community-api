package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.util.Optional;

@Ignore
@ActiveProfiles("oracle")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class OffenderRepositoryTest {

    @LocalServerPort
    int port;

    @Autowired
    private OffenderRepository offenderRepository;

    @Test
    public void canSeeTheOffenderTable() {
        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(2500000534L);
        System.out.println(maybeOffender);
    }


}
