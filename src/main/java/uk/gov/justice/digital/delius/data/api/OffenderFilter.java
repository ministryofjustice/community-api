package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@ApiModel(description = "Offender Filter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class OffenderFilter implements Specification<Offender> {
    @ApiModelProperty(value = "Filter by offenders that are active, i.e. offenders on a sentence that probation has an interest in.", example = "true")
    private boolean includeActiveOnly;

    @ApiModelProperty(value = "Include deleted offenders", example = "true")
    private boolean includedDeleted;

    // TODO
//    @ApiModelProperty(value = "Filter by offenders that were active on the supplied date", position = 2, example = "2017-10-31", dataType = "LocalDate")
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//    private LocalDate activeDate;

    @Override
    public Predicate toPredicate(@NotNull Root<Offender> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (!includedDeleted) {
            predicateBuilder.add(criteriaBuilder.equal(root.get("softDeleted"), 0));
        }

        if (includeActiveOnly) {
            predicateBuilder.add(criteriaBuilder.equal(root.get("currentDisposal"), 1));
        }
        return criteriaBuilder.and(predicateBuilder.build().toArray(new Predicate[0]));
    }
}
