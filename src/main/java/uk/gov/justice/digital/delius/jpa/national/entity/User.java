package uk.gov.justice.digital.delius.jpa.national.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USER_")
public class User {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "STAFF_ID")
    private Long staffId;

    @Column(name = "FORENAME")
    private String forename;

    @Column(name = "FORENAME2")
    private String forename2;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "DISTINGUISHED_NAME")
    private String distinguishedName;

    @Column(name = "EXTERNAL_PROVIDER_EMPLOYEEFLAG")
    private String externalProviderEmployeeFlag;

    @Column(name = "EXTERNAL_PROVIDER_ID")
    private Long externalProviderId;

    @Column(name = "PRIVATE")
    private Long privateFlag;

    @Column(name = "ORGANISATION_ID")
    private Long organisationId;

    @Column(name = "SC_PROVIDER_ID")
    private Long scProviderId;

    @JoinColumn(name = "USER_ID")
    @OneToMany
    private List<Exclusion> exclusions;

    @JoinColumn(name = "USER_ID")
    @OneToMany
    private List<Restriction> restrictions;

    @JoinTable(name = "PROBATION_AREA_USER",
            joinColumns = {@JoinColumn(name = "USER_ID")},
            inverseJoinColumns = {@JoinColumn(name = "PROBATION_AREA_ID")})
    @OneToMany
    private List<ProbationArea> probationAreas;

    public boolean isExcludedFrom(Long offenderId) {
        return exclusions.stream().anyMatch(exclusion -> exclusion.getOffenderId().equals(offenderId) && exclusion.isActive());
    }

    public boolean isRestrictedUserFor(Long offenderId) {
        return restrictions.stream().anyMatch(restriction -> restriction.getOffenderId().equals(offenderId) && restriction.isActive());
    }

}
