package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Time;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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
