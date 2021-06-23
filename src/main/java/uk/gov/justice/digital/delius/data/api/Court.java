package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(name = "Unique id of this court", example = "2500000001")
    private Long courtId;

    @ApiModelProperty(name = "Unique code of this court", example = "SHEFMC")
    private String code;

    @ApiModelProperty(name = "Selectable flag", example = "true")
    private Boolean selectable;

    @ApiModelProperty(name = "Court name", example = "Sheffield Magistrates Court")
    private String courtName;

    @ApiModelProperty(name = "Telephone number", example = "0300 047 0777")
    private String telephoneNumber;

    @ApiModelProperty(name = "Fax number", example = "0114 2756 373")
    private String fax;

    @ApiModelProperty(name = "Building name", example = "Sheffield Magistrates Court")
    private String buildingName;

    @ApiModelProperty(name = "Street", example = "Castle Street")
    private String street;

    @ApiModelProperty(name = "Locality", example = "Sheffield City Centre")
    private String locality;

    @ApiModelProperty(name = "Town", example = "Sheffield")
    private String town;

    @ApiModelProperty(name = "County", example = "South Yorkshire")
    private String county;

    @ApiModelProperty(name = "Postcode", example = "S3 8LU")
    private String postcode;

    @ApiModelProperty(name = "Country", example = "England")
    private String country;

    @ApiModelProperty(name = "Court type id", example = "310")
    private Long courtTypeId;

    @ApiModelProperty(name = "Created date & time", example = "2014-05-29T21:50:16")
    private LocalDateTime createdDatetime;

    @ApiModelProperty(name = "Last updated date & time", example = "2014-05-29T21:50:16")
    private LocalDateTime lastUpdatedDatetime;

    @ApiModelProperty(name = "Probation area id", example = "1500001001")
    private Long probationAreaId;

    @ApiModelProperty(name = "Secure email address", example = "example@example.com")
    private String secureEmailAddress;

    @ApiModelProperty(name = "Probation area (aka provider)")
    private KeyValue probationArea;

    @ApiModelProperty(name = "Type of the court")
    private KeyValue courtType;
}
