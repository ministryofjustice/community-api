package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Court;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

@Component
public class CourtTransformer {


    public Court courtOf(uk.gov.justice.digital.delius.jpa.standard.entity.Court court) {
        return Court.builder()
            .courtId(court.getCourtId())
            .code(court.getCode())
            .selectable(ynToBoolean(court.getSelectable()))
            .courtName(court.getCourtName())
            .telephoneNumber(court.getTelephoneNumber())
            .fax(court.getFax())
            .buildingName(court.getBuildingName())
            .street(court.getStreet())
            .locality(court.getLocality())
            .town(court.getTown())
            .county(court.getCounty())
            .postcode(court.getPostcode())
            .country(court.getCountry())
            .courtTypeId(court.getCourtTypeId())
            .createdDatetime(court.getCreatedDatetime())
            .lastUpdatedDatetime(court.getLastUpdatedDatetime())
            .probationAreaId(court.getProbationAreaId())
            .secureEmailAddress(court.getSecureEmailAddress())
            .build();
    }


}
