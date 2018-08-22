package uk.gov.justice.digital.delius.helpers;

import java.util.function.Predicate;

public interface FluentHelper {

    static <T> Predicate<T> not(Predicate<T> predicate) {

        return predicate.negate();
    }

}
