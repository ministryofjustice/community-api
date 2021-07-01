package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @ApiModelProperty(name = "Address start date", example = "2021-06-10", required = true)
    private LocalDate from;

    @ApiModelProperty(name = "Address end date", example = "2021-07-10")
    private LocalDate to;

    @ApiModelProperty(name = "The address is of no fixed abode", example = "true")
    private Boolean noFixedAbode;

    @ApiModelProperty(name = "Notes about this address", example = "Some address notes")
    private String notes;

    @ApiModelProperty(name = "Building number", example = "32")
    private String addressNumber;

    @ApiModelProperty(name = "Building name", example = "HMPPS Digital Studio")
    private String buildingName;

    @ApiModelProperty(name = "Street name", example = "Scotland Street")
    private String streetName;

    @ApiModelProperty(name = "District", example = "Sheffield City Centre")
    private String district;

    @ApiModelProperty(name = "Town or city", example = "Sheffield")
    private String town;

    @ApiModelProperty(name = "County", example = "South Yorkshire")
    private String county;

    @ApiModelProperty(name = "Postcode", example = "S3 7BS")
    private String postcode;

    @ApiModelProperty(name = "Telephone number", example = "0123456789")
    private String telephoneNumber;

    @ApiModelProperty(name = "Address status")
    private KeyValue status;

    @ApiModelProperty(name = "Address type")
    private KeyValue type;

    @ApiModelProperty(name = "Address type is verified/evidenced", example = "true")
    private Boolean typeVerified;

    @ApiModelProperty(name = "If address previously assessed then the latest assessment date and time", example = "2021-06-11T12:00:00")
    private LocalDateTime latestAssessmentDate;

    @ApiModelProperty(name = "Date and time that this address was created", example = "2021-06-11T13:00:00")
    private LocalDateTime createdDatetime;

    @ApiModelProperty(name = "Date and time that this address was last updated", example = "2021-06-11T14:00:00")
    private LocalDateTime lastUpdatedDatetime;
}
