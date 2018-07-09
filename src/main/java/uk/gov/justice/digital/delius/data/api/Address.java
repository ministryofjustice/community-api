package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Address {
    @ApiModelProperty(required = true)
    private LocalDate from;
    private LocalDate to;
    private Boolean noFixedAbode;
    private String notes;
    private String addressNumber;
    private String buildingName;
    private String streetName;
    private String district;
    private String town;
    private String county;
    private String postcode;
    private String telephoneNumber;
}
