package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReferralSentRequest {
    @ApiModelProperty(required = true)
    private String probationAreaCode;

    @ApiModelProperty(required = true)
    private String referralType;

    @ApiModelProperty(required = true)
    private String staffCode;

    @ApiModelProperty(required = true)
    private String teamCode;

    @ApiModelProperty(required = true)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate date;
}
