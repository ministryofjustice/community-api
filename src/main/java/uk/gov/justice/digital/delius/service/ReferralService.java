package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;

@Service
@AllArgsConstructor
public class ReferralService {

    private final ContactRepository contactRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final StaffRepository staffRepository;
    private final OffenderRepository offenderRepository;
    private final TeamRepository teamRepository;
    private final ProbationAreaRepository probationAreaRepository;

    public static final String MSG_OFFENDER_NOT_FOUND = "Offender ID not found for CRN %s";
    public static final String CONTACT_TYPE_NOT_FOUND = "Contact type not found for code %s";
    public static final String STAFF_NOT_FOUND = "Staff not found for code %s";
    public static final String TEAM_NOT_FOUND = "Team not found for code %s";
    public static final String PROBATION_AREA_NOT_FOUND = "Probation area not found for code %s";
    public static final String OFFICE_LOCATION_NOT_FOUND = "Office location not found for code %s";

    @Transactional
    public void createReferralSent(final String crn,
                                   final ReferralSentRequest referralSent) {

        var offender = offenderRepository.findByCrn(crn)
            .orElseThrow(() -> new NotFoundException(String.format(MSG_OFFENDER_NOT_FOUND, crn)));
        var contactType = contactTypeRepository
            .findByCode(referralSent.getReferralType())
            .orElseThrow(() -> new NotFoundException(String.format(CONTACT_TYPE_NOT_FOUND, referralSent.getReferralType())));
        var staff = staffRepository
            .findByOfficerCode(referralSent.getStaffCode())
            .orElseThrow(() -> new NotFoundException(String.format(STAFF_NOT_FOUND, referralSent.getStaffCode())));
        var team = teamRepository.findByCode(referralSent.getTeamCode())
            .orElseThrow(() -> new NotFoundException(String.format(TEAM_NOT_FOUND, referralSent.getTeamCode())));
        var probationArea = probationAreaRepository.findByCode(referralSent.getProbationAreaCode())
            .orElseThrow(() -> new NotFoundException(String.format(PROBATION_AREA_NOT_FOUND, referralSent.getProbationAreaCode())));

        contactRepository
            .save(uk.gov.justice.digital.delius.jpa.standard.entity.Contact.builder()
                  .offenderId(offender.getOffenderId())
                  .contactDate(referralSent.getDate())
                  .contactType(contactType)
                  .staff(staff)
                  .probationArea(probationArea)
                  .teamProviderId(team.getTeamId())
                  .team(team)
                  .staffEmployeeId(staff.getStaffId())
                  .notes(referralSent.getNotes())
                  .build());
    }
}
