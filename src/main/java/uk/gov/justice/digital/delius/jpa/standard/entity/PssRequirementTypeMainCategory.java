package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "R_PSS_RQMNT_TYPE_MAIN_CATEGORY")
public class PssRequirementTypeMainCategory {
    @Id
    @Column(name = "PSS_RQMNT_TYPE_MAIN_CAT_ID")
    private Long requirementTypeMainCategoryId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;
}
