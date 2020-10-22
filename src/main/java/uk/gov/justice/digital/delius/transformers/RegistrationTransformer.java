package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.jpa.standard.entity.Deregistration;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.RegisterType;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

public class RegistrationTransformer {

    public static Registration registrationOf(uk.gov.justice.digital.delius.jpa.standard.entity.Registration registration) {
        final Predicate<Deregistration> hasDeregistered = notUsed -> convertToBoolean(registration.getDeregistered());

        return Registration.builder()
                .registrationId(registration.getRegistrationId())
                .offenderId(registration.getOffenderId())
                .endDate(Optional.ofNullable(registration.getLatestDeregistration())
                        .filter(hasDeregistered)
                        .map(Deregistration::getDeregistrationDate)
                        .orElse(null))
                .startDate(registration.getRegistrationDate())
                .nextReviewDate(registration.getNextReviewDate())
                .reviewPeriodMonths(registration.getRegisterType().getRegisterReviewPeriod())
                .offenderId(registration.getOffenderId())
                .register(keyValueOf(registration.getRegisterType().getRegisterTypeFlag()))
                .type(typeOf(registration.getRegisterType()))
                .riskColour(registration.getRegisterType().getColour())
                .registeringOfficer(ContactTransformer.staffOf(registration.getRegisteringStaff()))
                .registeringTeam(ContactTransformer.teamOf(registration.getRegisteringTeam()))
                .registeringProbationArea(probationAreaOf(registration.getRegisteringTeam().getProbationArea()))
                .notes(registration.getRegistrationNotes())
                .registerLevel(Optional.ofNullable(registration.getRegisterLevel()).map(RegistrationTransformer::keyValueOf).orElse(null))
                .registerCategory(Optional.ofNullable(registration.getRegisterCategory()).map(RegistrationTransformer::keyValueOf).orElse(null))
                .warnUser(Optional.ofNullable(ynToBoolean(registration.getRegisterType().getAlertMessage())).orElse(false))
                .active(!convertToBoolean(registration.getDeregistered()))
                .deregisteringOfficer(Optional.ofNullable(registration.getLatestDeregistration())
                        .filter(hasDeregistered)
                        .map(deregistration -> ContactTransformer.staffOf(deregistration.getDeregisteringStaff()))
                        .orElse(null))
                .deregisteringTeam(Optional.ofNullable(registration.getLatestDeregistration())
                        .filter(hasDeregistered)
                        .map(deregistration -> ContactTransformer.teamOf(deregistration.getDeregisteringTeam()))
                        .orElse(null))
                .deregisteringProbationArea(Optional.ofNullable(registration.getLatestDeregistration())
                        .filter(hasDeregistered)
                        .map(deregistration -> probationAreaOf(deregistration.getDeregisteringTeam().getProbationArea()))
                        .orElse(null))
                .deregisteringNotes(Optional.ofNullable(registration.getLatestDeregistration())
                        .filter(hasDeregistered)
                        .map(Deregistration::getDeregisteringNotes)
                        .orElse(null))
                .numberOfPreviousDeregistrations(registration.getDeregistrations().size())
            .build();
    }

    private static KeyValue keyValueOf(StandardReference register) {
        return KeyValue.builder().code(register.getCodeValue()).description(register.getCodeDescription()).build();
    }
    private static KeyValue typeOf(RegisterType registerType) {
        return KeyValue.builder().code(registerType.getCode()).description(registerType.getDescription()).build();
    }

    private static KeyValue probationAreaOf(ProbationArea probationArea) {
        return KeyValue.builder()
                .code(probationArea.getCode())
                .description(probationArea.getDescription())
                .build();
    }


}
