package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.IAPSEvent;
import uk.gov.justice.digital.delius.jpa.standard.repository.IAPSEventRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.anEvent;

@ExtendWith(MockitoExtension.class)
public class IAPSNotificationServiceTest {
    @Mock
    private IAPSEventRepository iapsEventRepository;

    private IAPSNotificationService iapsNotificationService;

    @BeforeEach
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