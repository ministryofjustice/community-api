package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BUSINESS_INTERACTION")
public class BusinessInteraction {
    @Id
    @Column(name = "BUSINESS_INTERACTION_ID")
    private Long businessInteractionId;
    @Column(name = "BUSINESS_INTERACTION_CODE")
    private String businessInteractionCode;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "ENABLED_DATE")
    private LocalDateTime enabledDate;
    @Column(name = "AUDIT_INTERACTION_PARAMETER")
    private LocalDateTime auditInteractionParameter;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;

}
