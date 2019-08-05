package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerPk implements Serializable {
    @Id
    @Column(name = "TRUST_PROVIDER_FLAG")
    private Long trustProviderFlag;

    @Id
    @Column(name = "STAFF_EMPLOYEE_ID")
    private Long staffEmployeeId;
}
