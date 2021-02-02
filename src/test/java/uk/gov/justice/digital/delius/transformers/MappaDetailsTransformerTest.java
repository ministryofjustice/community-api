package uk.gov.justice.digital.delius.transformers;

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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class MappaDetailsTransformerTest {

    @Test
    void transformsRegistrationToMappDetails() {
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
            .softDeleted(0L)
            .build();

        MappaDetails mappaDetails = MappaDetailsTransformer.transform(registration);

        assertThat(mappaDetails.getLevel()).isEqualTo(2);
        assertThat(mappaDetails.getCategory()).isEqualTo(2);
        assertThat(mappaDetails.getStartDate()).isEqualTo(LocalDate.of(2021, 2 , 1));
        assertThat(mappaDetails.getReviewDate()).isEqualTo(LocalDate.of(2021, 5 , 1));
        assertThat(mappaDetails.getTeam()).isEqualTo(KeyValue.builder().code("N02AAM").description("OMIC OMU A").build());
        assertThat(mappaDetails.getOfficer()).isEqualTo(StaffHuman.builder().code("N02AAMU").forenames("Unallocated").surname("Staff").build());
        assertThat(mappaDetails.getProbationArea()).isEqualTo(KeyValue.builder().code("NPS").description("NPS North East").build());


    }
}
