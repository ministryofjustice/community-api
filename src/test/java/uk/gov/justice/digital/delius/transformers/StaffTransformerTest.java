package uk.gov.justice.digital.delius.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;

public class StaffTransformerTest {
    private StaffTransformer staffTransformer = new StaffTransformer(new TeamTransformer());

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
    public void teamsTakenFromStaff() {
        assertThat(StaffTransformer.staffDetailsOf(
                                        aStaff()
                                        .toBuilder()
                                        .teams(ImmutableList.of(aTeam(), aTeam()))
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

}
