package uk.gov.justice.digital.delius.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.IAPSEventRepository;

/*
 * Service for recording notifications to IAPS - Intermin Accredited Programme System
 * for Intervention managers
 */
@Service
public class IAPSNotificationService {
    private final IAPSEventRepository iapsEventRepository;

    public IAPSNotificationService(IAPSEventRepository iapsEventRepository) {
        this.iapsEventRepository = iapsEventRepository;
    }

    @Transactional
    public void notifyEventUpdated(Event event) {
        iapsEventRepository.findById(event.getEventId())
                .ifPresent(iapsEvent -> iapsEvent.setIapsFlag(IAPSFlag.UPDATED.getValue()));
    }

    @Getter
    private enum IAPSFlag {
        CLEARED(0L),
        UPDATED(1L),
        DELETED(2L);
        final Long value;

        IAPSFlag(Long value) {
            this.value = value;
        }
    }
}
