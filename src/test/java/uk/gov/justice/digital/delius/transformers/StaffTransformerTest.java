package uk.gov.justice.digital.delius.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

public class StaffTransformerTest {
    private StaffTransformer staffTransformer = new StaffTransformer(new TeamTransformer());


    @Test
    public void staffNameDetailsTakenFromStaff() {
        assertThat(staffTransformer.staffDetailsOf(
                aStaff().toBuilder().forename("John").surname("Smith").forname2("George").build())
                .getStaff()).isEqualTo(
                        Human.builder().forenames("John George").surname("Smith").build());
    }

    @Test
    public void staffCodeTakenFromStaff() {
        assertThat(staffTransformer.staffDetailsOf(
                aStaff().toBuilder().forename("John").surname("Smith").officerCode("XXXXX").build())
                .getStaffCode()).isEqualTo("XXXXX");
    }

    @Test
    public void teamsTakenFromStaff() {
        assertThat(staffTransformer.staffDetailsOf(aStaff().toBuilder()
                .teams(ImmutableList.of(Team.builder().build(), Team.builder().build())).build())
                .getTeams()).hasSize(2);
    }

}
