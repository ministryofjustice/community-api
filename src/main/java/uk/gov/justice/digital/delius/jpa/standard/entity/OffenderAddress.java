package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private Long softDeleted;

    @ManyToOne
    @JoinColumn(name = "ADDRESS_STATUS_ID")
    private StandardReference addressStatus;

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

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ADDRESS_ID")
    @Where(clause = "SOFT_DELETED != 1")
    private List<PersonalCircumstance> personalCircumstances;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ADDRESS_ID")
    @Where(clause = "SOFT_DELETED != 1")
    private List<AddressAssessment> addressAssessments;
}