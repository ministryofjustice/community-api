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
public class Custody {

    @ApiModelProperty(value = "Human readable id of the prison booking, AKA book number", example = "V74111")
    private String bookingNumber;
    @ApiModelProperty(value = "Institution where the offender currently resides")
    private Institution institution;
    @ApiModelProperty(value = "Key sentence dates of particular interest to custody")
    private CustodyRelatedKeyDates keyDates;
    @ApiModelProperty(value = "Custodial status")
    private KeyValue status;
    @ApiModelProperty(value = "Date when related sentence started")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate sentenceStartDate;


}
