package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.ContactableHuman;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.digital.delius.transformers.ProbationAreaTransformer.probationAreaOf;

public class StaffTransformer {

    public static StaffDetails staffDetailsOf(Staff staff) {
        return StaffDetails.builder()
                .staff(humanOf(staff))
                .staffCode(staff.getOfficerCode())
                .staffIdentifier(staff.getStaffId())
                .username(
                    Optional.ofNullable(staff.getUser()).map(User::getDistinguishedName).orElse(null))
                .teams(staff.getTeams().stream()
                        .map(TeamTransformer::teamOf)
                        .collect(Collectors.toList()))
                .probationArea(probationAreaOf(staff.getProbationArea(), false))
                .staffGrade(KeyValueTransformer.keyValueOf(staff.getGrade()))
                .build();
    }

    public static StaffDetails staffDetailsOnlyOf(Staff staff) {
        return StaffDetails.builder()
            .staff(humanOf(staff))
            .staffCode(staff.getOfficerCode())
            .staffIdentifier(staff.getStaffId())
            .username(
                Optional.ofNullable(staff.getUser()).map(User::getDistinguishedName).orElse(null))
            .staffGrade(KeyValueTransformer.keyValueOf(staff.getGrade()))
            .build();
    }

    public static String combinedMiddleNamesOf(String secondName, String thirdName) {
        Optional<String> maybeSecondName = Optional.ofNullable(secondName);
        Optional<String> maybeThirdName = Optional.ofNullable(thirdName);

        return Stream.of(maybeSecondName, maybeThirdName)
                .flatMap(Optional::stream)
                .collect(Collectors.joining(" "));
    }

    static Human humanOf(Staff staff) {
        return Human.builder()
                .forenames(combinedMiddleNamesOf(staff.getForename(), staff.getForname2()))
                .surname(staff.getSurname())
                .build();
    }
    static ContactableHuman contactableHumanOf(Staff staff, Optional<String> email, Optional<String> phoneNumber) {
        return ContactableHuman.builder()
            .forenames(combinedMiddleNamesOf(staff.getForename(), staff.getForname2()))
            .surname(staff.getSurname())
            .email(email.orElse(null))
            .phoneNumber(phoneNumber.orElse(null))
            .build();
    }

    public static StaffHuman staffOf(Staff staff) {
        return StaffHuman.builder()
            .forenames(combinedMiddleNamesOf(staff.getForename(), staff.getForname2()))
            .surname(staff.getSurname())
            .code(staff.getOfficerCode())
            .build();
    }
}

