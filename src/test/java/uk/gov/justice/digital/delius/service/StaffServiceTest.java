package uk.gov.justice.digital.delius.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.StaffTransformer;
import uk.gov.justice.digital.delius.transformers.TeamTransformer;

@RunWith(MockitoJUnitRunner.class)
public class StaffServiceTest {

        private StaffService staffService;

        @Mock
        private StaffRepository staffRepository;

        @Before
        public void setup() {
                staffService = new StaffService(staffRepository,
                                new OffenderTransformer(new ContactTransformer()),
                                new StaffTransformer(new TeamTransformer()));
        }

        @Test
        public void whenStaffMemberNotFoundReturnEmpty() {
                when(staffRepository.findByOfficerCode("ABC123")).thenReturn(Optional.empty());

                assertThat(staffService.getStaffDetails("ABC123")).isNotPresent();

        }

        @Test
        public void whenStaffMemberFoundReturnStaffDetails() {
                when(staffRepository.findByOfficerCode("ABC123")).thenReturn(Optional.of(aStaff()));

                assertThat(staffService.getStaffDetails("ABC123")).isPresent();
        }

}
