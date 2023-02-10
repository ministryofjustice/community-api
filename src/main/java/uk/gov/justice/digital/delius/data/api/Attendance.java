package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {

    @Schema(required = true)
    private Long contactId;

    @Schema(required = true)
    private LocalDate attendanceDate;

    @Schema(required = true)
    private boolean attended;

    @Schema(required = true )
    private boolean complied;

    private String outcome;

    private ContactTypeDetail contactType;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContactTypeDetail {

        @Schema(required = true)
        private String description;

        @Schema(required = true)
        private String code;

    }
}
