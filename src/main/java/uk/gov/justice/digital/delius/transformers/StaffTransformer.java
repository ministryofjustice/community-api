package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .surname(staff.getSurname()).build();
    }
}

