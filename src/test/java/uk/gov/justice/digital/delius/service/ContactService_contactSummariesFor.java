package uk.gov.justice.digital.delius.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactDateRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;

@ExtendWith(MockitoExtension.class)
public class ContactService_contactSummariesFor {
    @Mock private ContactRepository contactRepository;
    @Mock private ContactDateRepository contactDateRepository;
    @Mock private ContactTypeRepository contactTypeRepository;
    @InjectMocks private ContactService subject;

    @Captor
    private ArgumentCaptor<Specification<Contact>> specificationCaptor;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    public void contactSummariesFor() {
        final var contacts = List.of(
            EntityHelper.aContact().toBuilder().contactId(1L).build(),
            EntityHelper.aContact().toBuilder().contactId(2L).build()
        );
        final var filter = ContactFilter.builder()
            .from(Optional.of(LocalDateTime.of(2021, 5, 26, 0, 0)))
            .to(Optional.of(LocalDateTime.of(2021, 6, 2, 0, 0)))
            .contactTypes(Optional.of(List.of("CT1", "CT2")))
            .build();
        when(contactRepository.findAll(specificationCaptor.capture(), pageableCaptor.capture()))
            .thenReturn(new PageImpl(contacts, PageRequest.of(10, 20), 1000));

        final var observed = subject.contactSummariesFor(123L, filter, 1, 10);

        Assertions.assertThat(specificationCaptor.getValue()).isEqualTo(filter.toBuilder().offenderId(123L).build());
        Assertions.assertThat(pageableCaptor.getValue()).isEqualTo(PageRequest.of(1, 10, Sort.by(DESC, "contactDate", "contactStartTime", "contactEndTime")));
        Assertions.assertThat(observed.getContent()).hasSize(2).extracting("contactId", Long.class).containsExactly(1L, 2L);
        Assertions.assertThat(observed.getNumber()).isEqualTo(10);
        Assertions.assertThat(observed.getSize()).isEqualTo(20);
        Assertions.assertThat(observed.getTotalElements()).isEqualTo(1000);
    }
}
