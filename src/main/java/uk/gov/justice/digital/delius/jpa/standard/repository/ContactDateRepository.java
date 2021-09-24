package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

import java.time.LocalDate;

/**
 * Repository of distinct contact dates.
 */
public interface ContactDateRepository {
    Page<LocalDate> findAll(Specification<Contact> spec, Pageable pageable);

    Long count(Specification<Contact> spec);
}
