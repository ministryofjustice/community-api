package uk.gov.justice.digital.delius.transformers;

public class OffenceIdTransformer {
    public static String mainOffenceIdOf(Long offenceId) {
        return String.format("%s%d", "M", offenceId);
    }

    public static String additionalOffenceIdOf(Long offenceId) {
        return String.format("%s%d", "A", offenceId);
    }
}
