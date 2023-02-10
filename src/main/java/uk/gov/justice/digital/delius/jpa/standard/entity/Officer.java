package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OFFICER")
@IdClass(OfficerPk.class)
public class Officer {

    @Id
    @Column(name = "TRUST_PROVIDER_FLAG")
    private Long trustProviderFlag;

    @Id
    @Column(name = "STAFF_EMPLOYEE_ID")
    private Long staffEmployeeId;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "FORENAME")
    private String forename;

    @Column(name = "FORENAME2")
    private String forename2;

}
