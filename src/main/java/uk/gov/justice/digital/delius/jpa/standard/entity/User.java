package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "USER_")
public class User {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @OneToOne(optional = true)
    @JoinColumn(name = "STAFF_ID")
    private Staff staff;

    @Column(name = "FORENAME")
    private String forename;

    @Column(name = "FORENAME2")
    private String forename2;

    @Column(name = "END_DATE")
    private LocalDate endDate;

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

}
