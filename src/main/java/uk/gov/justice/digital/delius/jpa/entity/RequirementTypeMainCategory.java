package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "R_RQMNT_TYPE_MAIN_CATEGORY")
public class RequirementTypeMainCategory {
    @Id
    @Column(name = "RQMNT_TYPE_MAIN_CATEGORY_ID")
    private Long requirementTypeMainCategoryId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;
}
