package uk.gov.justice.digital.delius.controller.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
    @Schema(description = "Http status code", example = "400", requiredMode = RequiredMode.REQUIRED)
    private Integer status;
    @Schema(description = "Reason for error", example = "Surname required")
    private String developerMessage;
}
