package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.AppointmentType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.OrderType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.RequiredOptional;
import uk.gov.justice.digital.delius.service.AppointmentService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AppointmentMetaControllerTest {
    @Mock
    private AppointmentService service;

    @InjectMocks
    private AppointmentMetaController subject;

    @Test
    public void gettingAllAppointmentTypes() {
        final var types = List.of(anAppointmentType("type1"), anAppointmentType("type2"));
        when(service.getAllAppointmentTypes()).thenReturn(types);

        final var observed = subject.getAllAppointmentTypes();

        assertThat(observed).isEqualTo(types);
    }

    private AppointmentType anAppointmentType(String type) {
        return AppointmentType.builder()
            .contactType(type)
            .description(String.format("description %s", type))
            .requiresLocation(RequiredOptional.REQUIRED)
            .orderTypes(List.of(OrderType.CJA_2003))
            .build();
    }
}
