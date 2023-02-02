package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.service.CaseloadService;
import uk.gov.justice.digital.delius.service.TeamService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamResourceTest {
    @Mock
    private TeamService service;

    @InjectMocks
    private TeamResource subject;

    @Test
    public void gettingStaffByTeamCode() {
        final var CODE = "some-team";
        StaffDetails staff1 = new StaffDetails();
        StaffDetails staff2 = new StaffDetails();
        final var staffHumanList = List.of(staff1,staff2);
        when(service.getAllStaff(CODE)).thenReturn(staffHumanList);

        final var observed = subject.getAllStaff(CODE);

        assertThat(observed).isSameAs(staffHumanList);
    }
}
