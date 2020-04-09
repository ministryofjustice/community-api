package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderDocumentDetail {
    public enum Type {
        OFFENDER_DOCUMENT("Offender related"),
        CONVICTION_DOCUMENT("Sentence related"),
        CPSPACK_DOCUMENT("Crown Prosecution Service case pack"),
        PRECONS_DOCUMENT("PNC previous convictions"),
        COURT_REPORT_DOCUMENT("Court report"),
        INSTITUTION_REPORT_DOCUMENT("Institution report"),
        ADDRESS_ASSESSMENT_DOCUMENT("Address assessment related document"),
        APPROVED_PREMISES_REFERRAL_DOCUMENT("Approved premises referral related document"),
        ASSESSMENT_DOCUMENT("Assessment document"),
        CASE_ALLOCATION_DOCUMENT("Case allocation document"),
        PERSONAL_CONTACT_DOCUMENT("Personal contact related document"),
        REFERRAL_DOCUMENT("Referral related document"),
        NSI_DOCUMENT("Non Statutory Intervention related document"),
        PERSONAL_CIRCUMSTANCE_DOCUMENT("Personal circumstance related document"),
        UPW_APPOINTMENT_DOCUMENT("Unpaid work appointment document"),
        CONTACT_DOCUMENT("Contact related document");

        private final String description;

        Type(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
    private String id;
    private String documentName;
    private String author;
    private KeyValue type;
    private String extendedDescription;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime createdAt;

    private KeyValue subType;
    private ReportDocumentDates reportDocumentDates;
}
