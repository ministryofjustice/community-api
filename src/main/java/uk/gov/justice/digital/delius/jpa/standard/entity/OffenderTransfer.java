package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "OFFENDER_TRANSFER")
public class OffenderTransfer {
    @Id
    @Column(name = "OFFENDER_TRANSFER_ID")
    private Long offenderTransferId;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "REQUEST_DATE")
    private LocalDate requestDate;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "NOTES")
    private String notes;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "TRANSFER_STATUS_DATE")
    private LocalDate transferStatusDate;
    @ManyToOne
    @JoinColumn(name = "ALLOCATION_REASON_ID")
    private StandardReference allocationReason;
}
