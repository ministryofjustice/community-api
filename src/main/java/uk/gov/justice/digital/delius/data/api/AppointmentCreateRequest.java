package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @Schema
    private Long nsiId;

    @Schema
    private Long requirementId;

    @NotNull
    @Schema(required = true)
    private String contactType;

    @NotNull
    @Schema(required = true)
    private OffsetDateTime appointmentStart;

    @NotNull
    @Schema(required = true)
    private OffsetDateTime appointmentEnd;

    @NotNull
    @Schema(required = true)
    private String officeLocationCode;

    @NotNull
    @Schema(required = true)
    private String notes;

    @NotNull
    @Schema(required = true)
    private String providerCode;

    @NotNull
    @Schema(required = true)
    private String teamCode;

    @NotNull
    @Schema(required = true)
    private String staffCode;

    @Schema
    private Boolean sensitive;

    @Schema
    private Boolean rarActivity;

    @Schema
    private String outcome;

    @Schema
    private String enforcement;
}