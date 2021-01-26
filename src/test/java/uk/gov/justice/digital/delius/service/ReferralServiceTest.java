package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;

import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReferralServiceTest {

    private ReferralService referralService;
    private ReferralSentRequest referralSent;
    private String crn;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactTypeRepository contactTypeRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ProbationAreaRepository probationAreaRepository;

    @BeforeEach
    public void setup() {
        crn = anOffender().getCrn();
        referralService = new ReferralService(contactRepository,
                                              contactTypeRepository,
                                              staffRepository,
                                              offenderRepository,
                                              teamRepository,
                                              probationAreaRepository);

        referralSent = new ReferralSentRequest("YSS",  // Probation Area
                                               "C116", // Contact Type
                                               "N00UATUW", // Staff Code
                                               "N00UAT",  // Team Code
                                               "A test note", // Notes
                                               LocalDate.now()); // Date

        var offender = Offender.builder().offenderId(1L).build();
        var contactType = ContactType.builder().contactTypeId(1L).build();
        var staff = Staff.builder().staffId(1L).build();
        var team = Team.builder().teamId(1L).build();
        var probationArea = ProbationArea.builder().probationAreaId(1L).build();

        when(offenderRepository.findByCrn(any(String.class))).thenReturn(Optional.of(offender));
        when(contactTypeRepository.findByCode(any(String.class))).thenReturn(Optional.of(contactType));
        when(staffRepository.findByOfficerCode(any(String.class))).thenReturn(Optional.of(staff));
        when(teamRepository.findByCode(any(String.class))).thenReturn(Optional.of(team));
        when(probationAreaRepository.findByCode(any(String.class))).thenReturn(Optional.of(probationArea));
    }

    @Test
    public void addReferralSentContactTest() {
        referralService.createReferralSent(crn, referralSent);
    }
}
