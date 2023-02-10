package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import jakarta.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@Builder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("COURT_REPORT")
public class CourtReportDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "COURT_REPORT_ID", insertable = false, updatable = false)
    @ManyToOne
    private CourtReport courtReport;

}
