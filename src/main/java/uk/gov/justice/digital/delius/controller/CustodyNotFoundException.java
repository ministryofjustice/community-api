package uk.gov.justice.digital.delius.controller;

import uk.gov.justice.digital.delius.jpa.standard.entity.Event;

public class CustodyNotFoundException extends BadRequestException {
    public CustodyNotFoundException(Event custodialEvent) {
        super(String.format("Expected custody record for event id %d could not be found ", custodialEvent.getEventId()));
    }
}
