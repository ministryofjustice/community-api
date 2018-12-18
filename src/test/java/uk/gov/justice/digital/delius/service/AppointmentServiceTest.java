package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.transformers.AppointmentTransformer;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentServiceTest {
    @Mock
    private ContactRepository contactRepository;

    @Captor
    private ArgumentCaptor<Specification<Contact>> specificationArgumentCaptor;
    @Captor
    private ArgumentCaptor<Sort> sortArgumentCaptor;
    private AppointmentService service;

    @Before
    public void before(){
        service = new AppointmentService(contactRepository, new AppointmentTransformer(new ContactTransformer()));
        when(contactRepository.findAll(Matchers.any(Specification.class), Matchers.any(Sort.class))).thenReturn(ImmutableList.of());
    }

    @Test
    public void appointmentsSortedByContactDateDescending() {
        service.appointmentsFor(1L, AppointmentFilter.builder().build());

        verify(contactRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

        assertThat(sortArgumentCaptor.getValue().getOrderFor("contactDate")).isNotNull();
        assertThat(sortArgumentCaptor.getValue().getOrderFor("contactDate").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}