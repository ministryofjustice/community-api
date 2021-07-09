package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ADDRESS")
public class Address {
    @Id
    @Column(name = "ADDRESS_ID")
    private Long addressId;

    @Column(name = "ADDRESS_NUMBER")
    private String addressNumber;

    @Column(name = "BUILDING_NAME")
    private String buildingName;

    @Column(name = "COUNTY")
    private String county;

    @Column(name = "DISTRICT")
    private String district;

    @Column(name = "POSTCODE")
    private String postcode;

    @Column(name = "STREET_NAME")
    private String streetName;

    @Column(name = "TELEPHONE_NUMBER")
    private String telephoneNumber;

    @Column(name = "TOWN_CITY")
    private String townCity;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
}
