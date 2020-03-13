package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@ApiModel(description = "Attendance Wrapper")
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attendances {
    @ApiModelProperty(value = "List of Attendances")
    private final List<Attendance> attendances;

    // Needed for serialisation to keep final field
    private Attendances () {
        super();
        this.attendances = new ArrayList<>();
    }
}
