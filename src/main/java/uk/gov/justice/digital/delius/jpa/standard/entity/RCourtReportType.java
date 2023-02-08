package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "R_COURT_REPORT_TYPE")
public class RCourtReportType {
    public static final List<String> PRE_SENTENCE_REPORT_TYPES = List.of(
        "CJF", // Pre-Sentence Report - Fast
        "CJO", // Pre-Sentence Report - Oral
        "CJS", // Pre-Sentence Report - Standard
        "PSA"  // PSR - Addendum
    );

    @Id@Column(name = "COURT_REPORT_TYPE_ID")
    private Long courtReportTypeId;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "SELECTABLE")
    private String selectable;
    @Column(name = "DELIVERED")
    private String delivered;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "REQUESTED")
    private String requested;
    @Column(name = "FORM_30_CODE")
    private String form30Code;
    @Column(name = "PSR")
    private String psr;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "PRIVATE_TRANSFER")
    private String privateTransfer;

}
