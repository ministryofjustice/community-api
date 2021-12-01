package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAFF_GRADE_ID")
    private StandardReference grade;
}
