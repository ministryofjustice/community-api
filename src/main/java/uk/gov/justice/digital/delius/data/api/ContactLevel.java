package uk.gov.justice.digital.delius.data.api;

/**
 * The primary entity type that a contact can be related to.
 */
public enum ContactLevel {
    /**
     * Contact is only related to an offender.
     */
    OFFENDER,

    /**
     * Contact is related to an offender & conviction.
     */
    CONVICTION,

    /**
     * Contact is related to an offender, conviction & requirement.
     */
    REQUIREMENT,

    /**
     * Contact is related to an offender, conviction & nsi.
     */
    NSI
}
