package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
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

    @Mock
    private CaseloadService caseloadService;

    @InjectMocks
    private TeamResource subject;

    @Test
    public void gettingTeamOfficeLocations() {
        final var CODE = "some-team";
        final var locations = List.of(anOfficeLocation(), anOfficeLocation());
        when(service.getAllOfficeLocations(CODE)).thenReturn(locations);

        final var observed = subject.getAllOfficeLocations(CODE);

        assertThat(observed).isSameAs(locations);
    }

    @Test
    public void gettingCaseload() {
        final var code = "123456";
        final var caseload = Caseload.builder()
            .managedOffenders(Set.of(ManagedOffenderCrn.builder().offenderCrn("A123456").build()))
            .supervisedOrders(Set.of(ManagedEventId.builder().eventId(123L).build()))
            .build();
        when(caseloadService.getCaseloadByTeamCode(eq(code), any())).thenReturn(Optional.of(caseload));

        final var observed = subject.getCaseloadForTeam(code);

        assertThat(observed).isSameAs(caseload);
    }

    private static OfficeLocation anOfficeLocation() {
        return OfficeLocation.builder()
            .code("ASP_ASH")
            .description("Ashley House Approved Premises")
            .buildingName("Ashley House")
            .buildingNumber("14")
            .streetName("Somerset Street")
            .townCity("Bristol")
            .county("Somerset")
            .postcode("BS2 8NB")
            .build();
    }
}
