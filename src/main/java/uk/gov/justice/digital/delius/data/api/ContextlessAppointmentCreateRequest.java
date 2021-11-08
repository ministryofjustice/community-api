package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessAppointmentCreateRequest {

    // Fields used to identify associated Referral/Nsi
    @NotNull
    @ApiModelProperty(required = true)
    private String contractType;

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime referralStart;

    @ApiModelProperty
    private UUID referralId;

    // Fields used for creating the appointment
    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime appointmentStart;

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime appointmentEnd;

    @ApiModelProperty
    private String officeLocationCode;

    @NotNull
    @ApiModelProperty(required = true)
    private String notes;

    @NotNull
    @ApiModelProperty(required = true)
    private Boolean countsTowardsRarDays;

    @ApiModelProperty
    private String attended;

    @ApiModelProperty
    private Boolean notifyPPOfAttendanceBehaviour;
}