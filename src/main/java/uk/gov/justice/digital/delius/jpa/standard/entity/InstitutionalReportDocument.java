package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("INSTITUTIONAL_REPORT")
public class InstitutionalReportDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "INSTITUTIONAL_REPORT_ID", insertable = false, updatable = false)
    @ManyToOne
    private InstitutionalReport institutionalReport;

}
