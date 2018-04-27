package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Time;

@Data
@Entity
@Table(name = "R_INSTITUTION")
@IdClass(RInstitutionPK.class)
public class RInstitution {
    @Id
    @Column(name = "INSTITUTION_ID")
    private Long institutionId;
    @Id
    @Column(name = "ESTABLISHMENT")
    private String establishment;

    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "SELECTABLE")
    private String selectable;
    @Column(name = "INSTITUTION_NAME")
    private String institutionName;
    @Column(name = "FAX_NUMBER")
    private String faxNumber;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_DATETIME")
    private Time createdDatetime;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private Time lastUpdatedDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @OneToOne
    @JoinColumn(name = "ESTABLISHMENT_TYPE_ID")
    private StandardReference establishmentType;
    @Column(name = "IMMIGRATION_REMOVAL_CENTRE")
    private String immigrationRemovalCentre;
    @Column(name = "NOMIS_CDE_CODE")
    private String nomisCdeCode;
    @Column(name = "PRIVATE")
    private Long privateFlag;

}
