package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationReview {
    @JsonFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Date the registration was reviewed")
    private LocalDate reviewDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Date the next registration review is due")
    private LocalDate reviewDateDue;
    @Schema(description = "Notes attached to the registration review")
    private String notes;
    @Schema(description = "Probation team that reviewed the registration")
    private KeyValue reviewingTeam;
    @Schema(description = "Probation office that reviewed the registration")
    private StaffHuman reviewingOfficer;
    @Schema(description = "If the review has been completed")
    private boolean completed;
}
