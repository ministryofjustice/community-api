package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.transformers.AppointmentTransformer;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
public class AppointmentService {

    private final ContactRepository contactRepository;
    private final AppointmentTransformer appointmentTransformer;

    @Autowired
    public AppointmentService(ContactRepository contactRepository, AppointmentTransformer appointmentTransformer) {
        this.contactRepository = contactRepository;
        this.appointmentTransformer = appointmentTransformer;
    }

    public List<Appointment> appointmentsFor(Long offenderId, AppointmentFilter filter) {
        return AppointmentTransformer.appointmentsOf(
                contactRepository.findAll(
                        filter.toBuilder().offenderId(offenderId).build(),
                        Sort.by(DESC, "contactDate")));
    }

}
