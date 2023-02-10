package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Human readable id of the prison booking, AKA book number", example = "V74111")
    private String bookingNumber;
    @Schema(description = "Institution where the offender currently resides")
    private Institution institution;
    @Schema(description = "Key sentence dates of particular interest to custody")
    private CustodyRelatedKeyDates keyDates;
    @Schema(description = "Custodial status")
    private KeyValue status;
    @Schema(description = "Date when related sentence started")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate sentenceStartDate;


}
