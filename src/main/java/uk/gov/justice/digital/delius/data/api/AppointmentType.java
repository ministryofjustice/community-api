package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentType {
    @NotNull
    @ApiModelProperty(name = "Contact type", example = "CHVS", position = 1)
    private String contactType;

    @NotNull
    @ApiModelProperty(name = "Description", example = "Home Visit to Case (NS)", position = 2)
    private String description;

    @NotNull
    @ApiModelProperty(name = "Requires location", example = "REQUIRED", position = 3)
    private RequiredOptional requiresLocation;

    @ApiModelProperty(name = "Does this appointment type represent a national standard appointment")
    private Boolean nationalStandard;

    @ApiModelProperty(name = "Appointment can be used on the whole order")
    private Boolean wholeOrderLevel;

    @ApiModelProperty(name = "Appointment can be used at the offender level")
    private Boolean offenderLevel;

    @NotNull
    @JsonInclude
    @ApiModelProperty(
        name = "Order types appropriate for this appointment type",
        example = "[\"LEGACY\", \"CJA\"]",
        position = 4
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
