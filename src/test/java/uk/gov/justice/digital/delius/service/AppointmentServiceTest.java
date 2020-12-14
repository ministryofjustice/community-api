package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {
    @Mock
    private ContactRepository contactRepository;

    @Captor
    private ArgumentCaptor<Specification<Contact>> specificationArgumentCaptor;
    @Captor
    private ArgumentCaptor<Sort> sortArgumentCaptor;
    private AppointmentService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void before(){
        service = new AppointmentService(contactRepository);
        when(contactRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(ImmutableList.of());
    }

    @Test
    public void appointmentsSortedByContactDateDescending() {
        service.appointmentsFor(1L, AppointmentFilter.builder().build());

        verify(contactRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

        assertThat(sortArgumentCaptor.getValue().getOrderFor("contactDate")).isNotNull();
        assertThat(sortArgumentCaptor.getValue().getOrderFor("contactDate").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
