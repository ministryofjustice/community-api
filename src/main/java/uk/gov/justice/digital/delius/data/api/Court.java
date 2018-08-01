package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Court {
    private Long courtId;
    private String code;
    private Boolean selectable;
    private String courtName;
    private String telephoneNumber;
    private String fax;
    private String buildingName;
    private String street;
    private String locality;
    private String town;
    private String county;
    private String postcode;
    private String country;
    private Long courtTypeId;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastUpdatedDatetime;
    private Long probationAreaId;
    private String secureEmailAddress;
}
