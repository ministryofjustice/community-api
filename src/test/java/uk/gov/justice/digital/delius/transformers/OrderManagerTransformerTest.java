package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderManagerTransformerTest {

    @Test
    void orderManagerOf() {
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity())).isNotNull();
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getName()).isEqualTo("forename forename2 surname");
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getStaffCode()).isEqualTo("10001");
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getOfficerId()).isEqualTo(10002);
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getTeamId()).isEqualTo(10003);
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getProbationAreaId()).isEqualTo(10004);
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getDateStartOfAllocation()).isEqualTo(LocalDate.of(2021,4,1).atStartOfDay());
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getDateEndOfAllocation()).isEqualTo(LocalDate.of(2021,5,1).atStartOfDay());
        assertThat(OrderManagerTransformer.orderManagerOf(aOrderManagerEntity()).getGradeCode()).isEqualTo("123");

    }

    private OrderManager aOrderManagerEntity(){
        return OrderManager.builder()

            .staff(Staff.builder().staffId(10001L).forename("forename").surname("surname")
                .forname2("forename2").officerCode("10001")
                .grade(StandardReference.builder()
                    .codeValue("123").build()).build())
            .orderManagerId(10002L)
            .allocationDate(LocalDate.of(2021, 4,1).atStartOfDay())
            .endDate(LocalDate.of(2021,5,1).atStartOfDay())
            .team(Team.builder()
                .startDate(LocalDate.of(2021, 4,1))
                .endDate(LocalDate.of(2021,5,1)).teamId(10003L).build())
            .probationArea(ProbationArea.builder().probationAreaId(10004L).build()).build();
    }
}