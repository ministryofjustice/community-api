package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OFFENDER_ADDRESS")
public class OffenderAddress {

    @Id
    @Column(name = "OFFENDER_ADDRESS_ID")
    private Long offenderAddressID;

    @Column(name = "OFFENDER_ID")
    private Long offenderID;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "SOFT_DELETED")
    private boolean softDeleted;

    @ManyToOne
    @JoinColumn(name = "ADDRESS_STATUS_ID", insertable = false, updatable = false)
    private StandardReference addressStatus;

    @Column(name = "ADDRESS_STATUS_ID")
    private Long addressStatusID;

    @Column(name = "NO_FIXED_ABODE")
    private String noFixedAbode;

    @Lob
    @Column(name = "NOTES")
    private String notes;

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

}