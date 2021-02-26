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
    private String providerCode;

    @ApiModelProperty(required = true)
    private String referralType;

    @ApiModelProperty(required = true)
    private String staffCode;

    @ApiModelProperty(required = true)
    private String teamCode;

    private String notes;

    @ApiModelProperty(required = true)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate date;

    private String nsiType;

    private String nsiSubType;

    private Long convictionId;

    private Long requirementId;

    private String nsiStatus;

    private String nsiNotes;

    private String intendedProvider;


}
