package uk.gov.justice.digital.delius.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

@RunWith(MockitoJUnitRunner.class)
public class StaffServiceTest {

        private StaffService staffService;

        @Mock
        private StaffRepository staffRepository;

        @Before
        public void setup() {
                staffService = new StaffService(staffRepository,
                                new OffenderTransformer(new ContactTransformer()));
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


        @Test
        public void staffNameDetailsTakenFromStaff() {
                when(staffRepository.findByOfficerCode("ABC123"))
                                .thenReturn(
                                        Optional.of(
                                                aStaff()
                                                .toBuilder()
                                                .forename("John")
                                                .surname("Smith")
                                                .forname2("George")
                                                .build()));

                assertThat(staffService.getStaffDetails("ABC123").get().getStaff())
                .isEqualTo(
                        Human
                        .builder()
                        .forenames("John George")
                        .surname("Smith")
                        .build());        
        }

        @Test
        public void staffCodeTakenFromStaff() {
                when(staffRepository.findByOfficerCode("ABC123"))
                                .thenReturn(
                                        Optional.of(
                                                aStaff()
                                                .toBuilder()
                                                .forename("John")
                                                .surname("Smith")
                                                .officerCode("XXXXX")
                                                .build()));

                assertThat(staffService.getStaffDetails("ABC123").get().getStaffCode())
                .isEqualTo("XXXXX");        
        }


        @Test
        public void teamsTakenFromStaff() {
                when(staffRepository.findByOfficerCode("ABC123"))
                                .thenReturn(
                                        Optional.of(
                                                aStaff()
                                                .toBuilder()
                                                .teams(ImmutableList.of(
                                                        Team.builder().build(),
                                                        Team.builder().build()))
                                                .build()));

                assertThat(staffService.getStaffDetails("ABC123").get().getTeams()).hasSize(2);        
        }
}
