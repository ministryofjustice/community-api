package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "Date the registration was reviewed")
    private LocalDate reviewDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "Date the next registration review is due")
    private LocalDate reviewDateDue;
    @ApiModelProperty(value = "Notes attached to the registration review")
    private String notes;
    @ApiModelProperty(value = "Probation team that reviewed the registration")
    private KeyValue reviewingTeam;
    @ApiModelProperty(value = "Probation office that reviewed the registration")
    private StaffHuman reviewingOfficer;
    @ApiModelProperty(value = "If the review has been completed")
    private boolean completed;
}
