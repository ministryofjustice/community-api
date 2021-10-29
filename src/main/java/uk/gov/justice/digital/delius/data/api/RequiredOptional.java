package uk.gov.justice.digital.delius.data.api;

public enum RequiredOptional {
    /**
     * Value must be provided
     */
    REQUIRED,

    /**
     * Value may be provided
     */
    OPTIONAL,

    /**
     * Value must not be provided
     */
    NOT_REQUIRED
}
