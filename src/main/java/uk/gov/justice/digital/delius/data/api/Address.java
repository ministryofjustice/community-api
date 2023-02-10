package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "Address start date", example = "2021-06-10", required = true)
    private LocalDate from;

    @Schema(name = "Address end date", example = "2021-07-10")
    private LocalDate to;

    @Schema(name = "The address is of no fixed abode", example = "true")
    private Boolean noFixedAbode;

    @Schema(name = "Notes about this address", example = "Some address notes")
    private String notes;

    @Schema(name = "Building number", example = "32")
    private String addressNumber;

    @Schema(name = "Building name", example = "HMPPS Digital Studio")
    private String buildingName;

    @Schema(name = "Street name", example = "Scotland Street")
    private String streetName;

    @Schema(name = "District", example = "Sheffield City Centre")
    private String district;

    @Schema(name = "Town or city", example = "Sheffield")
    private String town;

    @Schema(name = "County", example = "South Yorkshire")
    private String county;

    @Schema(name = "Postcode", example = "S3 7BS")
    private String postcode;

    @Schema(name = "Telephone number", example = "0123456789")
    private String telephoneNumber;

    @Schema(name = "Address status")
    private KeyValue status;

    @Schema(name = "Address type")
    private KeyValue type;

    @Schema(name = "Address type is verified/evidenced", example = "true")
    private Boolean typeVerified;

    @Schema(name = "If address previously assessed then the latest assessment date and time", example = "2021-06-11T12:00:00")
    private LocalDateTime latestAssessmentDate;

    @Schema(name = "Date and time that this address was created", example = "2021-06-11T13:00:00")
    private LocalDateTime createdDatetime;

    @Schema(name = "Date and time that this address was last updated", example = "2021-06-11T14:00:00")
    private LocalDateTime lastUpdatedDatetime;
}
