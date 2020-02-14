package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustodyBookingNumber {
    @ApiModelProperty(value = "Prison Booking number to be set on the conviction. AKA bookNo, prison number ", example = "38339A")
    @NotBlank(message = "Missing a book number in bookingNumber")
    private String bookingNumber;
    @ApiModelProperty(value = "Sentence start date from prison used to match with probation conviction", example = "2020-02-28")
    @NotNull(message = "Missing a sentence start date in sentenceStartDate")
    private LocalDate sentenceStartDate;
}
