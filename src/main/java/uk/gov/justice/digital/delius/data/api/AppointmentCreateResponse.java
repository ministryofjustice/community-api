package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateResponse {

    @NotNull
    private Long appointmentId;
    private OffsetDateTime appointmentStart;
    private OffsetDateTime appointmentEnd;
    private String type;
    private String typeDescription;
}