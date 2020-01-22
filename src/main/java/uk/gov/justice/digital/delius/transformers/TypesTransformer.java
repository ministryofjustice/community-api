package uk.gov.justice.digital.delius.transformers;

import java.util.Optional;

public class TypesTransformer {
    public static Boolean ynToBoolean(String yn) {
        return Optional.ofNullable(yn).map("Y"::equalsIgnoreCase).orElse(null);
    }

    static Boolean zeroOneToBoolean(Long zeroOrOne) {
        return Optional.ofNullable(zeroOrOne).map(value -> value == 1).orElse(null);
    }

    public static boolean convertToBoolean(Long zeroOrNot) {
        return Optional.ofNullable(zeroOrNot).map(value -> value != 0).orElse(false);
    }
}
