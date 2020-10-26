package uk.gov.justice.digital.delius.controller.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @ApiModelProperty(required = true, value = "Http status code", example = "400")
    private Integer status;
    @ApiModelProperty(value = "Reason for error", example = "Surname required", position = 1)
    private String developerMessage;
}
