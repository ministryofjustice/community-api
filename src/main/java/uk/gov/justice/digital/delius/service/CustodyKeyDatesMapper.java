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

    public static String descriptionOf(String keyDateCode) {
        return Stream.of(Types.values()).filter(type -> type.getCode().equals(keyDateCode)).map(Types::getDescription)
                .findAny().orElseThrow();
    }


    enum Types {
        LICENCE_EXPIRY_DATE("LED", "Licence Expiry Date", ReplaceCustodyKeyDates::getLicenceExpiryDate),
        AUTOMATIC_CONDITIONAL_RELEASE_DATE("ACR", "Conditional Release Date", ReplaceCustodyKeyDates::getConditionalReleaseDate),
        PAROLE_ELIGIBILITY_DATE("PED", "Parole Eligibility Date", ReplaceCustodyKeyDates::getParoleEligibilityDate),
        SENTENCE_EXPIRY_DATE("SED", "Sentence Expiry Date", ReplaceCustodyKeyDates::getSentenceExpiryDate),
        EXPECTED_RELEASE_DATE("EXP", "Expected Release Date", ReplaceCustodyKeyDates::getExpectedReleaseDate),
        HDC_EXPECTED_DATE("HDE", "HDC Eligibility Date", ReplaceCustodyKeyDates::getHdcEligibilityDate),
        POST_SENTENCE_SUPERVISION_END_DATE("PSSED", "PSS End Date", ReplaceCustodyKeyDates::getPostSentenceSupervisionEndDate);

        private final Function<ReplaceCustodyKeyDates, LocalDate> supplier;
        private String code;
        private String description;

        Types(String code, String description, Function<ReplaceCustodyKeyDates, LocalDate> supplier) {
            this.code = code;
            this.description = description;
            this.supplier = supplier;
        }

        String getCode() {
            return code;
        }

        Optional<LocalDate> value(ReplaceCustodyKeyDates replaceCustodyKeyDates) {
            return Optional.ofNullable(supplier.apply(replaceCustodyKeyDates));
        }

        public String getDescription() {
            return description;
        }
    }
}
