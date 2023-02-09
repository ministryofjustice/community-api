package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustodyBookingNumber {
    @Schema(description = "Prison Booking number to be set on the conviction. AKA bookNo, prison number ", example = "38339A")
    @NotBlank(message = "Missing a book number in bookingNumber")
    private String bookingNumber;
    @Schema(description = "Sentence start date from prison used to match with probation conviction", example = "2020-02-28")
    @NotNull(message = "Missing a sentence start date in sentenceStartDate")
    private LocalDate sentenceStartDate;
}
