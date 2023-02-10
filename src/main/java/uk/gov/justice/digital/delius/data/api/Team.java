package uk.gov.justice.digital.delius.data.api;

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
public class Team {
    @Schema(description = "Team code", example = "C01T04")
    private String code;
    @Schema(description = "Team description", example = "OMU A")
    private String description;
    @Schema(description = "Team telephone, often not populated", required = false, example = "OMU A")
    private String telephone;
    @Schema(description = "Team email address", required = false, example = "first.last@digital.justice.gov.uk")
    private String emailAddress;
    @Schema(description = "Local Delivery Unit - provides a geographic grouping of teams")
    private KeyValue localDeliveryUnit;
    @Schema(description = "Team Type - provides a logical, not necessarily geographic, grouping of teams")
    private KeyValue teamType;
    @Schema(description = "Team's district")
    private KeyValue district;
    @Schema(description = "Team's borough")
    private KeyValue borough;
    @Schema(description = "Team's start date")
    private LocalDate startDate;
    @Schema(description = "Team's end date")
    private LocalDate endDate;
}
