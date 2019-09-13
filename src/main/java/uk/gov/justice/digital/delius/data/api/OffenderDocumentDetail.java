package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OffenderDocumentDetail {
    public enum Type {
        OFFENDER_DOCUMENT("Offender related"),
        CONVICTION_DOCUMENT("Sentence related"),
        CPSPACK_DOCUMENT("Crown Prosecution Service case pack"),
        PRECONS_DOCUMENT("PNC previous convictions"),
        COURT_REPORT_DOCUMENT("Court report"),
        INSTITUTION_REPORT_DOCUMENT("Institution report");

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
}
