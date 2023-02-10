package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Court {
    @Schema(name = "Unique id of this court", example = "2500000001")
    private Long courtId;

    @Schema(name = "Unique code of this court", example = "SHEFMC")
    private String code;

    @Schema(name = "Selectable flag", example = "true")
    private Boolean selectable;

    @Schema(name = "Court name", example = "Sheffield Magistrates Court")
    private String courtName;

    @Schema(name = "Telephone number", example = "0300 047 0777")
    private String telephoneNumber;

    @Schema(name = "Fax number", example = "0114 2756 373")
    private String fax;

    @Schema(name = "Building name", example = "Sheffield Magistrates Court")
    private String buildingName;

    @Schema(name = "Street", example = "Castle Street")
    private String street;

    @Schema(name = "Locality", example = "Sheffield City Centre")
    private String locality;

    @Schema(name = "Town", example = "Sheffield")
    private String town;

    @Schema(name = "County", example = "South Yorkshire")
    private String county;

    @Schema(name = "Postcode", example = "S3 8LU")
    private String postcode;

    @Schema(name = "Country", example = "England")
    private String country;

    @Schema(name = "Court type id", example = "310")
    private Long courtTypeId;

    @Schema(name = "Created date & time", example = "2014-05-29T21:50:16")
    private LocalDateTime createdDatetime;

    @Schema(name = "Last updated date & time", example = "2014-05-29T21:50:16")
    private LocalDateTime lastUpdatedDatetime;

    @Schema(name = "Probation area id", example = "1500001001")
    private Long probationAreaId;

    @Schema(name = "Secure email address", example = "example@example.com")
    private String secureEmailAddress;

    @Schema(name = "Probation area (aka provider)")
    private KeyValue probationArea;

    @Schema(name = "Type of the court")
    private KeyValue courtType;
}
