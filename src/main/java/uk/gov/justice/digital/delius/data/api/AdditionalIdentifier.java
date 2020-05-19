package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Additional Identifier. \nCurrent active values for the type are\n " +
        "LIFN\tLifer Number\n" +
        "OTHR\tOther Personal Identifier\n" +
        "SPNC\tScottish/Old PNC Number\n" +
        "NPNC\tVerified No PNC Date\n" +
        "VISO\tViSOR Number\n" +
        "PCRN\tOther Previous CRN\n" +
        "IMMN\tImmigration Number\n" +
        "YCRN\tYOT Identifier/CRN\n" +
        "APNC\tAdditional PNC\n" +
        "URN\tCPS Unique Reference Number\n" +
        "AI02\tPrevious Prison Number\n" +
        "DOFF\tDuplicate Offender CRN\n" +
        "NINO\tNational Insurance Number\n" +
        "DNOMS\tDuplicate NOMIS Number\n" +
        "Full list can be found calling \"/secure/referenceData/set/ADDITIONAL IDENTIFIER TYPE\""
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalIdentifier {
    @ApiModelProperty(value = "unique id of identifier", example = "23456789")
    private Long additionalIdentifierId;
    @ApiModelProperty(value = "value of identifier", example = "X1234")
    private String value;
    @ApiModelProperty(value = "identifier name and description")
    private KeyValue type;
}
