package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "R_AD_RQMNT_TYPE_MAIN_CATEGORY")
public class AdRequirementTypeMainCategory {
    @Id
    @Column(name = "AD_RQMNT_TYPE_MAIN_CATEGORY_ID")
    private Long adRequirementTypeMainCategoryId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @JoinColumn(name = "UNITS_ID")
    @OneToOne
    private StandardReference units;
}
