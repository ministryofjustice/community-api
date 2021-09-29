package uk.gov.justice.digital.delius.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory.REHABILITATION_ACTIVITY_REQUIREMENT_CODE;

@Data
@Builder(toBuilder = true)
public class ContactFilter implements Specification<Contact> {
    public static final String INCLUDE_TYPE_PREFIX = "TYPE_";
    public static final String INCLUDE_APPOINTMENTS = "APPOINTMENTS";

    @Data
    @AllArgsConstructor
    public static class ConvictionDatesFilter {
        @NotNull
        private Long convictionId;

        private Optional<LocalDate> from;

        private Optional<LocalDate> to;
    }

    @Builder.Default
    private Optional<List<String>> contactTypes = Optional.empty();

    @Builder.Default
    private Optional<LocalDateTime> from = Optional.empty();

    @Builder.Default
    private Optional<LocalDateTime> to = Optional.empty();

    @Builder.Default
    private Optional<LocalDate> contactDateFrom = Optional.empty();

    @Builder.Default
    private Optional<LocalDate> contactDateTo = Optional.empty();

    private Long offenderId;

    @Builder.Default
    private Optional<Boolean> appointmentsOnly = Optional.empty();

    @Builder.Default
    private Optional<Long> convictionId = Optional.empty();

    /**
     * Filters to the specified conviction ID or any offender level contact within the dates of the conviction.
     */
    @Builder.Default
    private Optional<ConvictionDatesFilter> convictionDatesOf = Optional.empty();

    @Builder.Default
    private Optional<Boolean> attended = Optional.empty();

    @Builder.Default
    private Optional<Boolean> complied = Optional.empty();

    @Builder.Default
    private Optional<Boolean> nationalStandard = Optional.empty();

    @Builder.Default
    private Optional<Boolean> outcome = Optional.empty();

    @Builder.Default
    private Optional<List<String>> include = Optional.empty();

    @Builder.Default
    private Optional<Boolean> rarActivity = Optional.empty();

    @Override
    public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        final var predicateBuilder = ImmutableList.<Predicate>builder()
            .add(cb.equal(root.get("offenderId"), offenderId));

        contactTypes.ifPresent(strings -> predicateBuilder.add(root.get("contactType").get("code").in(strings)));

        from.ifPresent(localDateTime -> predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("createdDateTime"), localDateTime)));

        to.ifPresent(localDateTime -> predicateBuilder.add(cb.lessThanOrEqualTo(root.get("createdDateTime"), localDateTime)));

        contactDateFrom.ifPresent(localDate -> predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("contactDate"), localDate)));

        contactDateTo.ifPresent(localDate -> predicateBuilder.add(cb.lessThanOrEqualTo(root.get("contactDate"), localDate)));

        appointmentsOnly.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("contactType").get("attendanceContact"), value)));

        convictionId.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("event").get("eventId"), value)));

        attended.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("attended"), value ? "Y" : "N")));

        complied.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("complied"), value ? "Y" : "N")));

        nationalStandard.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("contactType").get("nationalStandardsContact"), value)));

        outcome.ifPresent(value -> predicateBuilder.add(value ? cb.isNotNull(root.get("contactOutcomeType")) : cb.isNull(root.get("contactOutcomeType"))));

        rarActivity.filter(value -> value).ifPresent(value -> predicateBuilder.add(rarActivityOnlyFilter(root, cb)));
        List<Predicate> includePredicates = getIncludePredicates(root, query, cb);

        if (includePredicates.size() > 0 ) {
            predicateBuilder.add(cb.or(includePredicates.toArray(new Predicate[0])));
        }

        convictionDatesOf.map(value -> {
            final var eventId = root.get("event").get("eventId");
            final var eventEq = cb.equal(eventId, value.getConvictionId());
            return getConvictionDatesPredicate(root, cb, value)
                .map(offenderLevel -> cb.or(eventEq, cb.and(cb.isNull(eventId), offenderLevel)))
                .orElse(eventEq);
        }).ifPresent(predicateBuilder::add);

        return cb.and(predicateBuilder.build().toArray(new Predicate[0]));
    }

    private Optional<Predicate> getConvictionDatesPredicate(Root<Contact> root, CriteriaBuilder cb, ConvictionDatesFilter filter) {
        final var builder = ImmutableList.<Predicate>builder();
        if (filter.getFrom().isPresent() && filter.getTo().isPresent()) {
            builder.add(cb.between(root.get("contactDate"), filter.getFrom().get(), filter.getTo().get()));
        } else if (filter.getFrom().isPresent()) {
            builder.add(cb.greaterThanOrEqualTo(root.get("contactDate"), filter.getFrom().get()));
        } else filter.getTo().ifPresent(value -> builder.add(cb.lessThanOrEqualTo(root.get("contactDate"), value)));
        final var predicates = builder.build();
        return predicates.isEmpty() ? Optional.empty() : Optional.of(cb.and(predicates.toArray(new Predicate[0])));
    }

    private Predicate rarActivityOnlyFilter(Root<Contact> root, CriteriaBuilder cb) {
        final var requirement = root.join("requirement", JoinType.LEFT);
        final var rarViaRequirement = cb.and(
            cb.equal(requirement.get("softDeleted"), false),
            cb.equal(
                requirement.join("requirementTypeMainCategory", JoinType.LEFT).get("code"),
                REHABILITATION_ACTIVITY_REQUIREMENT_CODE
            )
        );

        final var nsi = root.join("nsi", JoinType.LEFT);
        final var nsiRequirement = nsi.join("rqmnt", JoinType.LEFT);
        final var rarViaNsi = cb.and(
            cb.equal(nsi.get("softDeleted"), false),
            cb.equal(nsiRequirement.get("softDeleted"), false),
            cb.equal(
                nsiRequirement.join("requirementTypeMainCategory", JoinType.LEFT).get("code"),
                REHABILITATION_ACTIVITY_REQUIREMENT_CODE
            )
        );

        return cb.and(
            cb.equal(root.get("rarActivity"), "Y"),
            cb.or(cb.isNull(root.get("attended")), cb.equal(root.get("attended"), "Y")),
            cb.or(rarViaNsi, rarViaRequirement)
        );
    }

    private List<Predicate> getIncludePredicates(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return include.map(value -> value.stream()
            .map(inc -> inc.trim().toUpperCase())
            .distinct().map(inc -> {
                if(inc.startsWith(INCLUDE_TYPE_PREFIX)) {
                    return cb.equal(root.get("contactType").get("code"), getIncludeValue(inc));
                } else if(inc.equals(INCLUDE_APPOINTMENTS)) {
                    return cb.equal(root.get("contactType").get("attendanceContact"), true);
                } else {
                    throw new BadRequestException("Unknown include parameter");
                }
        }).collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    private String getIncludeValue(final String includeParameter) {
        var values = includeParameter.split("_",2);

        if (values.length > 1) {
            return values[1];
        }
        return "";
    }
}