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
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "COURT")
public class Court {
    @Id
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
    private Long rowVersion;

    @Column(name = "COURT_TYPE_ID")
    private Long courtTypeId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

    @Column(name = "PROBATION_AREA_ID")
    private Long probationAreaId;

    @Column(name = "SECURE_EMAIL_ADDRESS")
    private String secureEmailAddress;
}
