package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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

    public List<Appointment> appointmentsFor(Long offenderId, AppointmentFilter filter) {
        return AppointmentTransformer.appointmentsOf(
                contactRepository.findAll(
                        filter.toBuilder().offenderId(offenderId).build(),
                        Sort.by(DESC, "contactDate")));
    }

    public void createAppointment(final String offenderCrn, final AppointmentCreateRequest appointment) {

            var offender = offenderRepository.findByCrn(offenderCrn);

            if (!offender.isPresent()) {
                throw new RuntimeException("Offender not found");
            }

            var contactType = contactTypeRepository.findByCode(appointment.getAppointmentType());

            if (!contactType.isPresent()) {
                throw new RuntimeException("Contact type not found");
            }

            var staff = staffRepository.findByOfficerCode(appointment.getStaffCode());

            if (!staff.isPresent()) {
                throw new RuntimeException("Staff not found");
            }

            var team = teamRepository.findByCode(appointment.getTeamCode());

            if (!team.isPresent()) {
                throw new RuntimeException("Team not found");
            }

            var probationArea = probationAreaRepository.findByCode(appointment.getProbationAreaCode());

            if (!probationArea.isPresent()) {
                throw new RuntimeException("Probation Area not found");
            }

            var contactBuilder =  uk.gov.justice.digital.delius.jpa.standard.entity.Contact.builder()
                .offenderId(offender.get().getOffenderId())
                .contactType(contactType.get())
                .contactStartTime(appointment.getAppointmentStartTime())
                .contactDate(appointment.getAppointmentDate())
                .contactEndTime(appointment.getAppointmentStartTime())
                .staff(staff.get())
                .team(team.get())
                .probationArea(probationArea.get())
                .staffEmployeeId(staff.get().getStaffId())
                .teamProviderId(team.get().getTeamId());

            if (appointment.getEventId() > 0) {
                var event = eventRepository.findById(appointment.getEventId());

                if (!event.isPresent()) {
                    throw new RuntimeException("Event not found");
                }
                contactBuilder.event(event.get());
            }

            if (StringUtils.isNotEmpty(appointment.getOfficeLocationCode())) {
                var officeLocation = officeLocationRepository.findByCode(appointment.getOfficeLocationCode());

                if (!officeLocation.isPresent()) {
                    throw new RuntimeException("Office location not found");
                }
                contactBuilder.officeLocation(officeLocation.get());
            }

        contactRepository.save(contactBuilder.build());
    }
}
