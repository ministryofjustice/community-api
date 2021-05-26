package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.OfficeLocation;

public class OfficeLocationTransformer {
    public static OfficeLocation officeLocationOf(uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation location) {
        return OfficeLocation.builder()
            .code(location.getCode())
            .description(location.getDescription())
            .buildingName(location.getBuildingName())
            .buildingNumber(location.getBuildingNumber())
            .streetName(location.getStreetName())
            .townCity(location.getTownCity())
            .county(location.getCounty())
            .postcode(location.getPostcode())
            .build();
    }
}
