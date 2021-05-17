package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "COURT")
@EntityListeners(AuditingEntityListener.class)
public class Court {
    @Id
    @SequenceGenerator(name = "COURT_ID_GENERATOR", sequenceName = "COURT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COURT_ID_GENERATOR")
    @Column(name = "COURT_ID")
    private Long courtId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "SELECTABLE")
    private String selectable;

    @Column(name = "COURT_NAME")
    private String courtName;

    @Column(name = "TELEPHONE_NUMBER")
    private String telephoneNumber;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "BUILDING_NAME")
    private String buildingName;

    @Column(name = "STREET")
    private String street;

    @Column(name = "LOCALITY")
    private String locality;

    @Column(name = "TOWN")
    private String town;

    @Column(name = "COUNTY")
    private String county;

    @Column(name = "POSTCODE")
    private String postcode;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

    @Column(name = "COURT_TYPE_ID", insertable = false, updatable = false)
    private Long courtTypeId;

    @ManyToOne
    @JoinColumn(name = "COURT_TYPE_ID")
    private StandardReference courtType;

    @Column(name = "CREATED_DATETIME")
    @CreatedDate
    private LocalDateTime createdDatetime;

    @Column(name = "CREATED_BY_USER_ID")
    @CreatedBy
    private Long createdByUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    @LastModifiedDate
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    @LastModifiedBy
    private Long lastUpdatedUserId;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

    @Column(name = "PROBATION_AREA_ID", updatable = false, insertable = false)
    private Long probationAreaId;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @ManyToOne
    private ProbationArea probationArea;

    @Column(name = "SECURE_EMAIL_ADDRESS")
    private String secureEmailAddress;
}
