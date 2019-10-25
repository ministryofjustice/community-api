package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.IAPSEvent;
import uk.gov.justice.digital.delius.jpa.standard.repository.IAPSEventRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.anEvent;

@RunWith(MockitoJUnitRunner.class)
public class IAPSNotificationServiceTest {
    @Mock
    private IAPSEventRepository iapsEventRepository;

    private IAPSNotificationService iapsNotificationService;

    @Before
    public void before() {
        iapsNotificationService = new IAPSNotificationService(iapsEventRepository);
    }

    @Test
    public void notifyEventUpdatedWhenPresent() {
        val iapsEvent = IAPSEvent
                .builder()
                .eventId(99L)
                .iapsFlag(0L)
                .build();
        Mockito.when(iapsEventRepository.findById(99L)).thenReturn(Optional.of(iapsEvent));

        iapsNotificationService.notifyEventUpdated(anEvent(99L));

        assertThat(iapsEvent.getIapsFlag()).isEqualTo(1L);
    }
}