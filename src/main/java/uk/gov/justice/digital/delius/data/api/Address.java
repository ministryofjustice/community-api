package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
public class Address {
    @ApiModelProperty(required = true)
    private LocalDate from;
    private LocalDate to;
    private Optional<Boolean> noFixedAbode;
    private Optional<String> notes;
    private Optional<String> addressNumber;
    private Optional<String> buildingName;
    private Optional<String> streetName;
    private Optional<String> district;
    private Optional<String> town;
    private Optional<String> county;
    private Optional<String> postcode;
    private Optional<String> telephoneNumber;
}
