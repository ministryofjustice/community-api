package uk.gov.justice.digital.delius.transformers;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOrderManager;

class OrderManagerTransformerTest {

    @Test
    void valuesAreMappedCorrectly() {
        val entity = anOrderManager();
        val observed = OrderManagerTransformer.orderManagerOf(entity);
        assertThat(observed).isNotNull();
        assertThat(observed.getName()).isEqualTo("John Smith");
        assertThat(observed.getStaffCode()).isEqualTo("A1234");
        assertThat(observed.getGradeCode()).isEqualTo("SPO");
        assertThat(observed.getOfficerId()).isEqualTo(entity.getOrderManagerId());
        assertThat(observed.getTeamId()).isEqualTo(entity.getTeam().getTeamId());
        assertThat(observed.getTeamCode()).isEqualTo(entity.getTeam().getCode());
        assertThat(observed.getProbationAreaId()).isEqualTo(entity.getProbationArea().getProbationAreaId());
        assertThat(observed.getProbationAreaCode()).isEqualTo(entity.getProbationArea().getCode());
        assertThat(observed.getDateStartOfAllocation()).isEqualTo(LocalDate.of(2021,4,1).atStartOfDay());
        assertThat(observed.getDateEndOfAllocation()).isEqualTo(LocalDate.of(2021,5,1).atStartOfDay());
    }

    @Test
    void nullStaffIsHandled() {
        val entity = anOrderManager().toBuilder().staff(null).build();
        val observed = OrderManagerTransformer.orderManagerOf(entity);
        assertThat(observed).isNotNull();
        assertThat(observed.getName()).isNull();
        assertThat(observed.getStaffCode()).isNull();
        assertThat(observed.getGradeCode()).isNull();
    }

    @Test
    void nullTeamIsHandled() {
        val entity = anOrderManager().toBuilder().team(null).build();
        val observed = OrderManagerTransformer.orderManagerOf(entity);
        assertThat(observed).isNotNull();
        assertThat(observed.getTeamId()).isNull();
        assertThat(observed.getTeamCode()).isNull();
    }
}