package uk.gov.justice.digital.delius.jpa.filters;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.delius.data.api.Appointment.Attended;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
        List<Predicate> predicates = new ArrayList<>();

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

        predicates.add(cb.equal(root.get("offenderId"), offenderId));

        from.ifPresent(localDate -> predicates.add(cb.greaterThanOrEqualTo(root.get("contactDate"), localDate)));

        to.ifPresent(localDate -> predicates.add(cb.lessThanOrEqualTo(root.get("contactDate"), localDate)));

        attended.ifPresent(attendedFlag -> predicates.add(mapAttended.apply(attendedFlag)));

        predicates.add(cb.and(cb.equal(root.get("contactType").get("attendanceContact"), true)));
        predicates.add(cb.and(cb.equal(root.get("softDeleted"), 0L)));

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}