package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableContactOutcomeTypes {

    private ContactOutcomeTypeRequired outcomeRequired;

    private List<ContactOutcomeTypeDetail> outcomeTypes;

    public enum ContactOutcomeTypeRequired {
        /**
         * A contact outcome is required if the contact date is in the past.
         */
        REQUIRED,

        /**
         * A contact outcome may be provided but is not required.
         */
        OPTIONAL,

        /**
         * A contact outcome cannot be provided for this contact type.
         */
        NOT_ALLOWED
    }
}
