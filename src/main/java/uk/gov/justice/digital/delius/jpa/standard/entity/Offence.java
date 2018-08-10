package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "R_OFFENCE")
public class Offence {

    @Id
    @Column(name = "OFFENCE_ID")
    private Long offenceId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ABBREVIATION")
    private String abbreviation;

    @Column(name = "MAIN_CATEGORY_CODE")
    private String mainCategoryCode;

    @Column(name = "SELECTABLE")
    private String selectable;

    @Column(name = "MAIN_CATEGORY_DESCRIPTION")
    private String mainCategoryDescription;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "MAIN_CATEGORY_ABBREVIATION")
    private String mainCategoryAbbreviation;

    @JoinColumn(name = "OGRS_OFFENCE_CATEGORY_ID")
    @OneToOne
    private StandardReference ogrsOffenceCategory;

    @Column(name = "SUB_CATEGORY_CODE")
    private String subCategoryCode;

    @Column(name = "SUB_CATEGORY_DESCRIPTION")
    private String subCategoryDescription;

    @Column(name = "FORM_20_CODE")
    private String form20Code;

    @Column(name = "SUB_CATEGORY_ABBREVIATION")
    private String subCategoryAbbreviation;

    @Column(name = "CJIT_CODE")
    private String cjitCode;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

}
