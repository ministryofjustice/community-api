package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "OFFICE_LOCATION")
public class OfficeLocation {
    @Id
    @Column(name = "OFFICE_LOCATION_ID")
    private Long officeLocationId;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "BUILDING_NAME")
    private String buildingName;
    @Column(name = "BUILDING_NUMBER")
    private String buildingNumber;
    @Column(name = "STREET_NAME")
    private String streetName;
    @Column(name = "TOWN_CITY")
    private String townCity;
    @Column(name = "COUNTY")
    private String county;
    @Column(name = "POSTCODE")
    private String postcode;
    @Column(name = "START_DATE")
    private LocalDate startDate;
    @Column(name = "END_DATE")
    private LocalDate endDate;
    @Column(name = "FAX_NUMBER")
    private String faxNumber;
    @Column(name = "TELEPHONE_NUMBER")
    private String telephoneNumber;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "NOTES")
    private String notes;
    @Column(name = "CONTACT_NAME")
    private String contactName;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "DISTRICT")
    private String district;


}
