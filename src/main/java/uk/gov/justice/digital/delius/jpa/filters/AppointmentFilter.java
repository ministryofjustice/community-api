package uk.gov.justice.digital.delius.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.delius.data.api.Appointment.Attended;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class AppointmentFilter implements Specification<Contact> {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private Optional<LocalDate> from = Optional.empty();
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private Optional<LocalDate> to = Optional.empty();
    @Builder.Default
    private Optional<Attended> attended = Optional.empty();
    private Long offenderId;

    @Override
    public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        final Function<Attended, Predicate> mapAttended = (attended) -> {
            switch (attended) {
                case ATTENDED:
                    return cb.equal(root.get("attended"), "Y");
                case UNATTENDED:
                    return cb.equal(root.get("attended"), "N");
                case NOT_RECORDED:
                    return cb.isNull(root.get("attended"));
            }

            throw new IllegalArgumentException("attended must match enum");
        };

        predicateBuilder.add(cb.equal(root.get("offenderId"), offenderId));

        from.ifPresent(localDate -> predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("contactDate"), localDate)));

        to.ifPresent(localDate -> predicateBuilder.add(cb.lessThanOrEqualTo(root.get("contactDate"), localDate)));

        attended.ifPresent(attendedFlag -> predicateBuilder.add(mapAttended.apply(attendedFlag)));

        predicateBuilder.add(cb.and(cb.equal(root.get("contactType").get("attendanceContact"), "Y")));
        predicateBuilder.add(cb.and(cb.equal(root.get("softDeleted"), 0L)));

        ImmutableList<Predicate> predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}