package uk.gov.justice.digital.delius.transformers;

import java.util.Optional;

class TypesTransformer {
    static Boolean ynToBoolean(String yn) {
        return Optional.ofNullable(yn).map("Y"::equalsIgnoreCase).orElse(null);
    }

    static Boolean zeroOneToBoolean(Long zeroOrOne) {
        return Optional.ofNullable(zeroOrOne).map(value -> value == 1).orElse(null);
    }
}
