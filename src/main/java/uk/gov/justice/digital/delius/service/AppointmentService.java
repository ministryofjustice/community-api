package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OfficeLocationRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;
import uk.gov.justice.digital.delius.transformers.AppointmentTransformer;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final ContactRepository contactRepository;
    private final OffenderRepository offenderRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final OfficeLocationRepository officeLocationRepository;
    private final StaffRepository staffRepository;
    private final TeamRepository teamRepository;
    private final ProbationAreaRepository probationAreaRepository;
    private final EventRepository eventRepository;

    public static final String MSG_OFFENDER_NOT_FOUND = "Offender ID not found for CRN %s";
    public static final String CONTACT_TYPE_NOT_FOUND = "Contact type not found for code %s";
    public static final String STAFF_NOT_FOUND = "Staff not found for code %s";
    public static final String TEAM_NOT_FOUND = "Team not found for code %s";
    public static final String PROBATION_AREA_NOT_FOUND = "Probation area not found for code %s";
    public static final String EVENT_NOT_FOUND = "Event not found for ID %s";
    public static final String OFFICE_LOCATION_NOT_FOUND = "Office location not found for code %s";

    public List<Appointment> appointmentsFor(Long offenderId, AppointmentFilter filter) {
        return AppointmentTransformer.appointmentsOf(
                contactRepository.findAll(
                        filter.toBuilder().offenderId(offenderId).build(),
                        Sort.by(DESC, "contactDate")));
    }

    public void createAppointment(final String offenderCrn, final Long eventId, final AppointmentCreateRequest appointment) {
            var offender = offenderRepository.findByCrn(offenderCrn).orElseThrow(() -> new NotFoundException(String.format(MSG_OFFENDER_NOT_FOUND, offenderCrn)));
            var contactType = contactTypeRepository.findByCode(appointment.getAppointmentType()).orElseThrow(() -> new NotFoundException(String.format(CONTACT_TYPE_NOT_FOUND, appointment.getAppointmentType())));
            var staff = staffRepository.findByOfficerCode(appointment.getStaffCode()).orElseThrow(() -> new NotFoundException(String.format(STAFF_NOT_FOUND, appointment.getStaffCode())));
            var team = teamRepository.findByCode(appointment.getTeamCode()).orElseThrow(() -> new NotFoundException(String.format(TEAM_NOT_FOUND, appointment.getTeamCode())));
            var probationArea = probationAreaRepository.findByCode(appointment.getProbationAreaCode()).orElseThrow(() -> new NotFoundException(String.format(PROBATION_AREA_NOT_FOUND, appointment.getProbationAreaCode())));
            var event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(String.format(EVENT_NOT_FOUND, eventId)));

            //TODO: Validate the event belongs to the offender
            //TODO: Validate the member of staff is an officer
            //TODO: Validate the member of staff belongs to the given team
            //TODO: Validate the team belongs to the given provider
            //TODO: Validate the location belongs to the given team

            var contactBuilder =  uk.gov.justice.digital.delius.jpa.standard.entity.Contact.builder()
                .offenderId(offender.getOffenderId())
                .event(event)
                .contactType(contactType)
                .contactStartTime(appointment.getAppointmentStartTime())
                .contactDate(appointment.getAppointmentDate())
                .contactEndTime(appointment.getAppointmentEndTime())
                .staff(staff)
                .team(team)
                .probationArea(probationArea)
                .staffEmployeeId(staff.getStaffId())
                .teamProviderId(team.getTeamId());


            if (StringUtils.isNotEmpty(appointment.getOfficeLocationCode())) {
                var officeLocation = officeLocationRepository.findByCode(appointment.getOfficeLocationCode()).orElseThrow(() -> new NotFoundException(String.format(OFFICE_LOCATION_NOT_FOUND, appointment.getOfficeLocationCode())));

                contactBuilder.officeLocation(officeLocation);
            }

        contactRepository.save(contactBuilder.build());
    }
}
