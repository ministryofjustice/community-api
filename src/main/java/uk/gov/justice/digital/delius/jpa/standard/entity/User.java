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
