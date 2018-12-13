package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

@Component
public class RegistrationTransformer {
    private final ContactTransformer contactTransformer;

    public RegistrationTransformer(ContactTransformer contactTransformer) {
        this.contactTransformer = contactTransformer;
    }

    public Registration registrationOf(uk.gov.justice.digital.delius.jpa.standard.entity.Registration registration) {
        final Predicate<Deregistration> hasDeregistered = notUsed -> convertToBoolean(registration.getDeregistered());

        return Registration.builder()
                .registrationId(registration.getRegistrationId())
                .offenderId(registration.getOffenderId())
                .endDate(Optional.ofNullable(registration.getDeregistration())
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
                .registeringOfficer(contactTransformer.staffOf(registration.getRegisteringStaff()))
                .registeringTeam(contactTransformer.teamOf(registration.getRegisteringTeam()))
                .registeringProbationArea(probationAreaOf(registration.getRegisteringTeam().getProbationArea()))
                .notes(registration.getRegistrationNotes())
                .registerLevel(Optional.ofNullable(registration.getRegisterLevel()).map(this::keyValueOf).orElse(null))
                .registerCategory(Optional.ofNullable(registration.getRegisterCategory()).map(this::keyValueOf).orElse(null))
                .warnUser(Optional.ofNullable(ynToBoolean(registration.getRegisterType().getAlertMessage())).orElse(false))
                .active(!convertToBoolean(registration.getDeregistered()))
                .deregisteringOfficer(Optional.ofNullable(registration.getDeregistration())
                        .filter(hasDeregistered)
                        .map(deregistration -> contactTransformer.staffOf(deregistration.getDeregisteringStaff()))
                        .orElse(null))
                .deregisteringTeam(Optional.ofNullable(registration.getDeregistration())
                        .filter(hasDeregistered)
                        .map(deregistration -> contactTransformer.teamOf(deregistration.getDeregisteringTeam()))
                        .orElse(null))
                .deregisteringProbationArea(Optional.ofNullable(registration.getDeregistration())
                        .filter(hasDeregistered)
                        .map(deregistration -> probationAreaOf(deregistration.getDeregisteringTeam().getProbationArea()))
                        .orElse(null))
                .deregisteringNotes(Optional.ofNullable(registration.getDeregistration())
                        .filter(hasDeregistered)
                        .map(Deregistration::getDeregisteringNotes)
                        .orElse(null))
            .build();
    }

    private KeyValue keyValueOf(StandardReference register) {
        return KeyValue.builder().code(register.getCodeValue()).description(register.getCodeDescription()).build();
    }
    private KeyValue typeOf(RegisterType registerType) {
        return KeyValue.builder().code(registerType.getCode()).description(registerType.getDescription()).build();
    }

    private KeyValue probationAreaOf(ProbationArea probationArea) {
        return KeyValue.builder()
                .code(probationArea.getCode())
                .description(probationArea.getDescription())
                .build();
    }


}
