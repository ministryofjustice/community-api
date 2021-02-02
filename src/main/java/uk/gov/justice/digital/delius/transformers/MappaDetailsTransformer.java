package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.jpa.standard.entity.Registration;

import java.util.Arrays;

import static java.lang.String.format;


public class MappaDetailsTransformer {


    public static MappaDetails mappaDetailsOf(Registration registration) {
        final var team = registration.getRegisteringTeam();
        final var staff = registration.getRegisteringStaff();
        final var probationArea = registration.getRegisteringTeam().getProbationArea();
        return MappaDetails.builder()
            .level(MappaLevel.toCommunityLevel(registration.getRegisterLevel().getCodeValue()))
            .category(MappaCategory.toCommunityCategory(registration.getRegisterCategory().getCodeValue()))
            .startDate(registration.getRegistrationDate())
            .reviewDate(registration.getNextReviewDate())
            .team(KeyValue.builder().code(team.getCode()).description(team.getDescription()).build())
            .officer(StaffHuman.builder().code(staff.getOfficerCode()).forenames(staff.getForename()).surname(staff.getSurname()).build())
            .probationArea(KeyValue.builder().code(probationArea.getCode()).description(probationArea.getDescription()).build())
            .notes(registration.getRegistrationNotes())
            .build();

    }

    enum MappaLevel {
        NOMINAL(0, "M0"),
        ONE(1, "M1"),
        TWO(2, "M2"),
        THREE(3, "M3");

        private final Integer communityValue;
        private final String deliusValue;

        MappaLevel(Integer communityValue, String deliusValue) {
            this.communityValue = communityValue;
            this.deliusValue = deliusValue;
        }

        static Integer toCommunityLevel(String deliusLevel) {
            return Arrays.stream(MappaLevel.values())
                .filter(level -> level.deliusValue.equals(deliusLevel))
                .findFirst()
                .map(level -> level.communityValue)
                .orElseThrow(() -> new IllegalArgumentException(format("Did not expect Delius MAPPA level %s", deliusLevel)));
        }
    }

    enum MappaCategory {
        NOMINAL(0, "M0"),
        ONE(1, "M1"),
        TWO(2, "M2"),
        THREE(3, "M3");

        private final Integer communityValue;
        private final String deliusValue;

        MappaCategory(Integer communityValue, String deliusValue) {
            this.communityValue = communityValue;
            this.deliusValue = deliusValue;
        }

        static Integer toCommunityCategory(String deliusCategory) {
            return Arrays.stream(MappaCategory.values())
                .filter(category -> category.deliusValue.equals(deliusCategory))
                .findFirst()
                .map(category -> category.communityValue)
                .orElseThrow(() -> new IllegalArgumentException(format("Did not expect Delius MAPPA category %s", deliusCategory)));
        }
    }

}

