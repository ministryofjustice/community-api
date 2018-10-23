package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Time;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "R_CIRCUMSTANCE_TYPE")
public class CircumstanceType {
    @Id@Column(name = "CIRCUMSTANCE_TYPE_ID")
    private long circumstanceTypeId;
    @Column(name = "CODE_VALUE")
    private String codeValue;
    @Column(name = "CODE_DESCRIPTION")
    private String codeDescription;
    @Column(name = "SELECTABLE")
    private String selectable;
    @Column(name = "ACTIVE_DUPLICATES")
    private String activeDuplicates;
    @Column(name = "ROW_VERSION")
    private long rowVersion;
    @Column(name = "CREATED_BY_USER_ID")
    private long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private Time createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private Time lastUpdatedDatetime;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "ENQUIRY")
    private Long enquiry;
    @Column(name = "SPG_INTEREST")
    private Long spgInterest;

}
