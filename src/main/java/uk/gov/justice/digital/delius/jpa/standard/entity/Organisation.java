package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.sql.Time;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Organisation {
    @Id
    @Column(name = "ORGANISATION_ID")
    private Long organisationId;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "CONTACT_NAME")
    private String contactName;
    @Column(name = "PRIVATE")
    private Long privateFlag;
    @Column(name = "START_DATE")
    private Time startDate;
    @Column(name = "END_DATE")
    private Time endDate;
    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private Time createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private Time lastUpdatedDatetime;

}
