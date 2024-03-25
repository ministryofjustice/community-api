package uk.gov.justice.digital.delius.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;

import java.util.List;

public class StaffTransformerTest {

    @Test
    public void staffNameDetailsTakenFromStaff() {
        assertThat(StaffTransformer.staffDetailsOf(
                                        aStaff()
                                            .toBuilder()
                                            .forename("John")
                                            .surname("Smith")
                                            .forname2("George")
                                            .build()).getStaff())
                    .isEqualTo(Human.builder().forenames("John George").surname("Smith").build());
    }

    @Test
    public void staffCodeTakenFromStaff() {
        assertThat(StaffTransformer.staffDetailsOf(
                                        aStaff()
                                            .toBuilder()
                                            .forename("John")
                                            .surname("Smith")
                                            .officerCode("XXXXX")
                                            .build()).getStaffCode())
                    .isEqualTo("XXXXX");
    }

    @Test
    public void staffIdentifierTakenFromStaff() {
        assertThat(StaffTransformer.staffDetailsOf(
                aStaff()
                        .toBuilder()
                        .forename("John")
                        .surname("Smith")
                        .officerCode("XXXXX")
                        .staffId(1L)
                        .build()).getStaffIdentifier())
                .isEqualTo(1L);
    }


    @Test
    public void teamsTakenFromStaff() {
        assertThat(StaffTransformer.staffDetailsOf(
                                        aStaff()
                                        .toBuilder()
                                        .teams(List.of(aTeam(), aTeam()))
                                        .build()).getTeams())
                    .hasSize(2);
    }

    @Test
    public void usernameCopiedWhenLinkedToUser() {
        assertThat(StaffTransformer.staffDetailsOf(
                                        aStaff()
                                        .toBuilder()
                                        .user(User.builder().distinguishedName("username").build())
                                        .build()).getUsername())
                      .isEqualTo("username");
    }

    @Test
    public void usernameNotCopiedWhenNotLinkedToUser() {
        assertThat(StaffTransformer.staffDetailsOf(aStaff()
                                                    .toBuilder()
                                                    .user(null)
                                                    .build()).getUsername())
            .isNull();
    }


    @Test
    public void probationAreaTakenFromStaff() {
        assertThat(StaffTransformer.staffDetailsOf(
            aStaff()).getProbationArea())
            .isEqualTo(ProbationArea.builder()
                .probationAreaId(1L)
                .code("NO2")
                .description("NPS North East")
                .nps(null)
                .organisation(KeyValue.builder().code(null).description(null).build())
                .institution(null)
                .teams(null)
                .build());
    }
}
