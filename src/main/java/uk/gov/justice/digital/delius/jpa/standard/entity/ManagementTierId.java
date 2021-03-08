package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ManagementTierId implements Serializable {
    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @ManyToOne
    @JoinColumn(name = "TIER_ID")
    private StandardReference tier;

    @Column(name = "DATE_CHANGED")
    private LocalDateTime dateChanged;
}
