package uk.gov.justice.digital.delius.data.filters;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@ApiModel(description = "Offender Filter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class OffenderFilter {
    @ApiModelProperty(value = "Filter by offenders that are active, i.e. offenders on a sentence that probation has an interest in.", example = "true")
    private boolean includeActiveOnly;

    @ApiModelProperty(value = "Include deleted offenders", example = "true")
    private boolean includeDeleted;

    @ApiModelProperty(value = "Filter by offenders that were active on the supplied date. Advised not to use this in conjunction with includeActiveOnly since that will effectively return offenders active of this supplied date and current date which is typically not what is required.", example = "2017-10-31", dataType = "LocalDate")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate activeDate;

}
