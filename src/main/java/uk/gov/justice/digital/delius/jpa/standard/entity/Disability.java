package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Disability {
    @Id@Column(name = "DISABILITY_ID")
    private Long disabilityId;
    @Column(name = "OFFENDER_ID")
    private Long offenderID;
    @JoinColumn(name = "DISABILITY_TYPE_ID")
    @OneToOne
    private StandardReference disabilityType;
    @Column(name = "START_DATE")
    private LocalDate startDate;
    @Column(name = "FINISH_DATE")
    private LocalDate finishDate;
    @Column(name = "NOTES")
    private String notes;
    @OneToMany(mappedBy = "disability", fetch = FetchType.LAZY)
    private List<Provision> provisions;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

}
