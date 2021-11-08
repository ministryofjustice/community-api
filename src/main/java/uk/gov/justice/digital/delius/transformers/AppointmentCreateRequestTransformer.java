package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.Requirement;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.digital.delius.transformers.AppointmentPatchRequestTransformer.getEnforcementReferToOffenderManager;
import static uk.gov.justice.digital.delius.transformers.AppointmentPatchRequestTransformer.getOutcomeType;

/*
    Activity can count towards RAR    Sentence has RAR requirement    RAR Activity    Contact Type
    Yes - service delivery            Yes                             TRUE            CRSAPT
    No  - initial assessment          Yes                             FALSE           CRSSAA
    Yes - service delivery            No                              null            CRSAPT
    No  - initial assessment          No                              null            CRSSAA
*/
public class AppointmentCreateRequestTransformer {

    public static AppointmentCreateRequest appointmentOf(final ContextlessAppointmentCreateRequest request,
                                                         final Nsi nsi,
                                                         final IntegrationContext context) {
        final var contactMapping = context.getContactMapping();
        final var nsiContainsRarRequirement = isRarRequirement(nsi.getRequirement(), context.getRequirementRehabilitationActivityType());

        final Optional<String> outcomeType = getOutcomeType(context, request.getAttended(), request.getNotifyPPOfAttendanceBehaviour());
        final Optional<String> enforcement = getEnforcementReferToOffenderManager(context, request.getNotifyPPOfAttendanceBehaviour());

        return AppointmentCreateRequest.builder()
                .nsiId(nsi.getNsiId())
                .contactType(request.getCountsTowardsRarDays() ?
                    contactMapping.getAppointmentRarContactType() :
                    contactMapping.getAppointmentNonRarContactType())
                .appointmentStart(request.getAppointmentStart())
                .appointmentEnd(request.getAppointmentEnd())
                .officeLocationCode(request.getOfficeLocationCode())
                .notes(request.getNotes())
                .providerCode(context.getProviderCode())
                .staffCode(context.getStaffCode())
                .teamCode(context.getTeamCode())
                .rarActivity(isRarActivity(nsiContainsRarRequirement, request.getCountsTowardsRarDays()))
                .outcome(outcomeType.orElse(null))
                .enforcement(enforcement.orElse(null))
                .build();
    }

    private static Boolean isRarActivity(final Boolean nsiContainsRarRequirement, final Boolean canCountTowardsRarDays) {
        if ( !nsiContainsRarRequirement )
            return null;
        return canCountTowardsRarDays ? true : false;
    }

    private static Boolean isRarRequirement(final Requirement requirement, final String requirementRehabilitationActivityType) {
        return ofNullable(requirement)
            .map(Requirement::getRequirementTypeMainCategory)
            .map(KeyValue::getCode)
            .map(code -> requirementRehabilitationActivityType.equals(code))
            .orElse(false);
    }
}
