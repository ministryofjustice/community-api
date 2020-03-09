package uk.gov.justice.digital.delius.service;

import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class CustodyKeyDatesMapper {
    public static List<String> custodyManagedKeyDates() {
        return Stream
                .of(Types.values())
                .map(Types::getCode)
                .collect(toList());
    }

    public static List<String> missingKeyDateTypesCodes(ReplaceCustodyKeyDates replaceCustodyKeyDates) {
        return Stream
                .of(Types.values())
                .filter(type -> type
                        .value(replaceCustodyKeyDates)
                        .isEmpty())
                .map(Types::getCode)
                .collect(toList());
    }

    public static Map<String, LocalDate> keyDatesOf(ReplaceCustodyKeyDates replaceCustodyKeyDates) {
        return Stream
                .of(Types.values())
                .map(type -> type
                        .value(replaceCustodyKeyDates).map( date -> Map.entry(type.getCode(), date)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    enum Types {
        LICENCE_EXPIRY_DATE("LED", ReplaceCustodyKeyDates::getLicenceExpiryDate),
        AUTOMATIC_CONDITIONAL_RELEASE_DATE("ACR", ReplaceCustodyKeyDates::getConditionalReleaseDate),
        PAROLE_ELIGIBILITY_DATE("PED", ReplaceCustodyKeyDates::getParoleEligibilityDate),
        SENTENCE_EXPIRY_DATE("SED", ReplaceCustodyKeyDates::getSentenceExpiryDate),
        EXPECTED_RELEASE_DATE("EXP", ReplaceCustodyKeyDates::getExpectedReleaseDate),
        HDC_EXPECTED_DATE("HDE", ReplaceCustodyKeyDates::getHdcEligibilityDate),
        POST_SENTENCE_SUPERVISION_END_DATE("PSSED", ReplaceCustodyKeyDates::getPostSentenceSupervisionEndDate);

        private final Function<ReplaceCustodyKeyDates, LocalDate> supplier;
        private String code;

        Types(String code, Function<ReplaceCustodyKeyDates, LocalDate> supplier) {
            this.code = code;
            this.supplier = supplier;
        }

        String getCode() {
            return code;
        }

        Optional<LocalDate> value(ReplaceCustodyKeyDates replaceCustodyKeyDates) {
            return Optional.ofNullable(supplier.apply(replaceCustodyKeyDates));
        }
    }
}
