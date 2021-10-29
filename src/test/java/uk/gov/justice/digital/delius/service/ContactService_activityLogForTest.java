package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactDateRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;

@ExtendWith(MockitoExtension.class)
public class ContactService_activityLogForTest {
    @Mock private ContactRepository contactRepository;
    @Mock private ContactDateRepository contactDateRepository;
    @InjectMocks private ContactService subject;

    @Test
    public void whenGettingEmptyActivityLog() {
        final var filter = ContactFilter.builder().build();
        final var pageable = PageRequest.of(0, 20, Sort.by(DESC, "contactDate"));
        when(contactDateRepository.findAll(eq(filter), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of()));

        final var observed = subject.activityLogFor(filter, 0, 20);

        assertThat(observed.getContent()).asList().isEmpty();
    }

    @Test
    public void whenGettingActivityLog() {
        final var dates = List.of(
            LocalDate.of(2021, 3, 2),
            LocalDate.of(2021, 3, 1)
        );
        final var filter = ContactFilter.builder().offenderId(10L).build();
        final var pageable = PageRequest.of(0, 20, Sort.by(DESC, "contactDate"));
        when(contactDateRepository.findAll(eq(filter), eq(pageable)))
            .thenReturn(new PageImpl<>(dates));

        final var contactFilter = ContactFilter.builder()
            .offenderId(10L)
            .contactDateFrom(Optional.of(dates.get(1)))
            .contactDateTo(Optional.of(dates.get(0)))
            .build();
        final var contacts = List.of(
            EntityHelper.aContact().toBuilder().contactDate(dates.get(0)).build(),
            EntityHelper.aContact().toBuilder().contactDate(dates.get(1)).build()
        );
        when(contactRepository.findAll(eq(contactFilter))).thenReturn(contacts);

        final var observed = subject.activityLogFor(filter, 0, 20);

        assertThat(observed.getContent()).asList()
            .hasSize(2)
            .extracting("date", LocalDate.class)
            .containsExactly(dates.toArray(new LocalDate[0]));
    }
}
