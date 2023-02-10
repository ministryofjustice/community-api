package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentType {
    @NotNull
    @Schema(name = "Contact type", example = "CHVS")
    private String contactType;

    @NotNull
    @Schema(name = "Description", example = "Home Visit to Case (NS)")
    private String description;

    @NotNull
    @Schema(name = "Requires location", example = "REQUIRED")
    private RequiredOptional requiresLocation;

    @Schema(name = "Does this appointment type represent a national standard appointment")
    private Boolean nationalStandard;

    @Schema(name = "Appointment can be used on the whole order")
    private Boolean wholeOrderLevel;

    @Schema(name = "Appointment can be used at the offender level")
    private Boolean offenderLevel;

    @NotNull
    @JsonInclude
    @Schema(
        name = "Order types appropriate for this appointment type",
        example = "[\"LEGACY\", \"CJA\"]"
    )
    private List<OrderType> orderTypes;

    private List<KeyValue> requirementTypeMainCategories;

    public enum OrderType {
        /**
         * A CJA 2003 or later order.
         * https://www.legislation.gov.uk/ukpga/2003/44/contents
         */
        CJA,

        /**
         * An order that predates CJA 2003.
         */
        LEGACY
    }

}
