package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.RegisterType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Registration;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.transformers.MappaDetailsTransformer.MappaCategory;
import uk.gov.justice.digital.delius.transformers.MappaDetailsTransformer.MappaLevel;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class MappaDetailsTransformerTest {

    @Test
    void transformsRegistrationToMappaDetails() {
        Registration registration = aRegistration();

        MappaDetails mappaDetails = MappaDetailsTransformer.mappaDetailsOf(registration);

        assertThat(mappaDetails.getLevel()).isEqualTo(2);
        assertThat(mappaDetails.getLevelDescription()).isEqualTo("MAPPA Level 2");
        assertThat(mappaDetails.getCategory()).isEqualTo(2);
        assertThat(mappaDetails.getCategoryDescription()).isEqualTo("MAPPA Cat 2");
        assertThat(mappaDetails.getStartDate()).isEqualTo(LocalDate.of(2021, 2 , 1));
        assertThat(mappaDetails.getReviewDate()).isEqualTo(LocalDate.of(2021, 5 , 1));
        assertThat(mappaDetails.getTeam()).isEqualTo(KeyValue.builder().code("N02AAM").description("OMIC OMU A").build());
        assertThat(mappaDetails.getOfficer()).isEqualTo(StaffHuman.builder().code("N02AAMU").forenames("Unallocated").surname("Staff").build());
        assertThat(mappaDetails.getProbationArea()).isEqualTo(KeyValue.builder().code("NPS").description("NPS North East").build());
        assertThat(mappaDetails.getNotes()).isEqualTo("X320741 registering MAPPA cat 2 level 2");
    }

    @Test
    @DisplayName("Unknown MAPPA category and level map to zero with original description")
    void unknownMappaCategoryAndLevel_MapToZeroWithOriginalDescription() {
        Registration registration = aRegistration().toBuilder()
            .registerLevel(StandardReference.builder().codeValue("UNKNOWN_LEVEL").codeDescription("Unknown level").build())
            .registerCategory(StandardReference.builder().codeValue("UNKNOWN_CATEGORY").codeDescription("Unknown category").build())
            .build();

        MappaDetails mappaDetails = MappaDetailsTransformer.mappaDetailsOf(registration);

        assertThat(mappaDetails.getLevel()).isEqualTo(0);
        assertThat(mappaDetails.getLevelDescription()).isEqualTo("Unknown level");
        assertThat(mappaDetails.getCategory()).isEqualTo(0);
        assertThat(mappaDetails.getCategoryDescription()).isEqualTo("Unknown category");
    }

    @Test
    @DisplayName("Null MAPPA category and level map to zero with missing description")
    void nullMappaCategoryAndLevel_MapToZeroWithMissingDescription() {
        Registration registration = aRegistration().toBuilder()
            .registerLevel(null)
            .registerCategory(null)
            .build();

        MappaDetails mappaDetails = MappaDetailsTransformer.mappaDetailsOf(registration);

        assertThat(mappaDetails.getLevel()).isEqualTo(0);
        assertThat(mappaDetails.getLevelDescription()).isEqualTo("Missing level");
        assertThat(mappaDetails.getCategory()).isEqualTo(0);
        assertThat(mappaDetails.getCategoryDescription()).isEqualTo("Missing category");
    }

    @Test
    void MappaLevel_toCommunityLevel() {
        assertThat(MappaLevel.toCommunityLevel("M0")).isEqualTo(0);
        assertThat(MappaLevel.toCommunityLevel("M1")).isEqualTo(1);
        assertThat(MappaLevel.toCommunityLevel("M2")).isEqualTo(2);
        assertThat(MappaLevel.toCommunityLevel("M3")).isEqualTo(3);
        assertThat(MappaLevel.toCommunityLevel("UNKNOWN")).isEqualTo(0);
    }

    @Test
    void MappaCategory_toCommunityLevel() {
        assertThat(MappaCategory.toCommunityCategory("X9")).isEqualTo(0);
        assertThat(MappaCategory.toCommunityCategory("M1")).isEqualTo(1);
        assertThat(MappaCategory.toCommunityCategory("M2")).isEqualTo(2);
        assertThat(MappaCategory.toCommunityCategory("M3")).isEqualTo(3);
        assertThat(MappaCategory.toCommunityCategory("M4")).isEqualTo(4);
        assertThat(MappaCategory.toCommunityCategory("UNKNOWN")).isEqualTo(0);
        assertThat(MappaCategory.toCommunityCategory("M0")).isEqualTo(0);
    }

    private Registration aRegistration() {
        Registration registration =
            Registration.builder()
            .registrationId(2500155758L)
            .offenderId(2500343964L)
            .registerType(RegisterType.builder().code("MAPP").description("MAPPA").build())
            .registrationDate(LocalDate.of(2021, 2, 1))
            .nextReviewDate(LocalDate.of(2021, 5, 1))
            .registrationNotes("X320741 registering MAPPA cat 2 level 2")
            .registeringTeam(Team.builder().code("N02AAM").description("OMIC OMU A").probationArea(ProbationArea.builder().code("NPS").description("NPS North East").build()).build())
            .registeringStaff(Staff.builder().officerCode("N02AAMU").forename("Unallocated").surname("Staff").build())
            .registerLevel(StandardReference.builder().codeValue("M2").codeDescription("MAPPA Level 2").build())
            .registerCategory(StandardReference.builder().codeValue("M2").codeDescription("MAPPA Cat 2").build())
            .registrationNotes("X320741 registering MAPPA cat 2 level 2")
            .softDeleted(0L)
            .build();
        return registration;
    }
}
