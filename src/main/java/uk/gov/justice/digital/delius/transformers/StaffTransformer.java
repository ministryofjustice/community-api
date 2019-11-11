package uk.gov.justice.digital.delius.transformers;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

@Component
public class StaffTransformer {

    private final TeamTransformer teamTransformer;

    public StaffTransformer(TeamTransformer teamTransformer) {
        this.teamTransformer = teamTransformer;
    }

    public StaffDetails staffDetailsOf(Staff staff) {
        return StaffDetails.builder().staff(Human.builder()
                .forenames(combinedMiddleNamesOf(staff.getForename(), staff.getForname2()))
                .surname(staff.getSurname()).build()).staffCode(staff.getOfficerCode())
                .teams(staff.getTeams().stream()
                        .map(teamTransformer::teamOf)
                        .collect(Collectors.toList()))
                .build();
    }

    public String combinedMiddleNamesOf(String secondName, String thirdName) {
        Optional<String> maybeSecondName = Optional.ofNullable(secondName);
        Optional<String> maybeThirdName = Optional.ofNullable(thirdName);

        return Stream.of(maybeSecondName, maybeThirdName)
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.joining(" "));
    }

}

