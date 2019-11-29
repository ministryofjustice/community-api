package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;

@Service
@AllArgsConstructor
public class CustodyService {

    public static class CustodyNotFoundException extends RuntimeException {
        public CustodyNotFoundException(Event custodialEvent) {
            super(String.format("Expected custody record for event id %d could not be found ", custodialEvent.getEventId()));
        }
    }

    // TODO DT-337 Flesh out this method - it should retrieve the disposal then custody from their respective repositories
    public Custody findCustodyFromCustodialEvent(Event custodialEvent) {
        return null;
    }
}
