package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "User's probation areas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAreas {
    @ApiModelProperty(value = "The home area for this user", example = "N02")
    private String homeProbationArea;
    @ApiModelProperty(value = "All probation areas the user can access AKA dataset", dataType = "List", example = "[\"N01\", \"N02\"]")
    private List<String> probationAreas;
}
