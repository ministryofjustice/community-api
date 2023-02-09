package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedOffender {
    @Schema(required = true)
    private String staffCode;
    @Schema(required = true)
    private Long staffIdentifier;
    @Schema(required = true)
    private Long offenderId;
    @Schema(required = true)
    private String nomsNumber;
    @Schema(required = true)
    private String crnNumber;
    @Schema(required = true)
    private String offenderSurname;
    @Schema(required = true)
    private boolean isCurrentRo;
    @Schema(required = true)
    private boolean isCurrentOm;
    @Schema(required = true)
    private boolean isCurrentPom;
    @Schema(required = true)
    private LocalDate omStartDate;
    @Schema(required = true)
    private LocalDate omEndDate;
}
