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
    @ApiModelProperty(required = true, value = "Status of Error Code", example = "400", position = 0)
    private Integer status;

    @ApiModelProperty(required = true, value = "Internal Error Code", example = "20012", position = 1)
    private Integer errorCode;

    @ApiModelProperty(required = true, value = "Error message information", example = "Offender Not Found", position = 2)
    private String userMessage;

    @ApiModelProperty(required = false, value = "Developer Information message", example = "System is down", position = 3)
    private String developerMessage;

    @ApiModelProperty(required = false, value = "Additional information about the error", example = "Hard disk failure", position = 4)
    private String moreInfo;
}
