package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.jpa.standard.entity.Registration;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Arrays;
import java.util.Optional;


public class MappaDetailsTransformer {


    public static MappaDetails mappaDetailsOf(Registration registration) {
        final var team = registration.getRegisteringTeam();
        final var staff = registration.getRegisteringStaff();
        final var probationArea = registration.getRegisteringTeam().getProbationArea();
        final var maybeLevel = Optional.ofNullable(registration.getRegisterLevel());
        final var maybeCategory = Optional.ofNullable(registration.getRegisterCategory());
        return MappaDetails.builder()
            .level(getLevel(maybeLevel))
            .levelDescription(getLevelDescription(maybeLevel))
            .category(getCategory(maybeCategory))
            .categoryDescription(getCategoryDescription(maybeCategory))
            .startDate(registration.getRegistrationDate())
            .reviewDate(registration.getNextReviewDate())
            .team(KeyValue.builder().code(team.getCode()).description(team.getDescription()).build())
            .officer(StaffHuman.builder().code(staff.getOfficerCode()).forenames(staff.getForename()).surname(staff.getSurname()).build())
            .probationArea(KeyValue.builder().code(probationArea.getCode()).description(probationArea.getDescription()).build())
            .notes(registration.getRegistrationNotes())
            .build();

    }

    private static String getCategoryDescription(Optional<StandardReference> maybeCategory) {
        return maybeCategory.map(StandardReference::getCodeDescription).orElse("Missing category");
    }

    private static Integer getCategory(Optional<StandardReference> maybeCategory) {
        return MappaCategory.toCommunityCategory(
            maybeCategory.map(StandardReference::getCodeValue).orElse("0")
        );
    }

    private static String getLevelDescription(Optional<StandardReference> maybeLevel) {
        return maybeLevel.map(StandardReference::getCodeDescription).orElse("Missing level");
    }

    private static Integer getLevel(Optional<StandardReference> maybeLevel) {
        return MappaLevel.toCommunityLevel(
            maybeLevel.map(StandardReference::getCodeValue).orElse("0")
        );
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
                .orElseGet(() -> NOMINAL.communityValue);
        }
    }

    enum MappaCategory {
        NOMINAL(0, "X9"),
        ONE(1, "M1"),
        TWO(2, "M2"),
        THREE(3, "M3"),
        FOUR(4, "M4");

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
                .orElseGet(() -> NOMINAL.communityValue);
        }
    }

}

