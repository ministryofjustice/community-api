package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aDistrict;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;

public class TeamTransformerTest {

    @Test
    public void willCopyCode() {
        assertThat(TeamTransformer.teamOf(
            aTeam().toBuilder().code("ABC").build()).getCode())
                .isEqualTo("ABC");

    }

    @Test
    public void willCopyDescription() {
        assertThat(TeamTransformer.teamOf(
            aTeam().toBuilder().description("My Description").build()).getDescription())
                .isEqualTo("My Description");

    }

    @Test
    public void willCopyTelephone() {
        assertThat(TeamTransformer.teamOf(aTeam().toBuilder().telephone("0114 222 4444").build()).getTelephone())
                .isEqualTo("0114 222 4444");

    }

    @Test
    public void willCopyDistrict() {
        assertThat(
                TeamTransformer
                        .teamOf(aTeam().toBuilder()
                                .district(aDistrict().toBuilder().code("AA")
                                        .description("My Description").build())
                                .build())
                        .getDistrict())
                                .isEqualTo(KeyValue.builder().code("AA")
                                        .description("My Description").build());
    }

    @Test
    public void willCopyDistrictAcrossAsLocalDeliveryUnit() {
        assertThat(TeamTransformer
                .teamOf(aTeam().toBuilder()
                        .district(aDistrict().toBuilder()
                                .code("LL")
                                .description("My Description").build())
                        .build())
                .getLocalDeliveryUnit()).isEqualTo(
                KeyValue.builder().code("LL").description("My Description").build());
    }

    @Test
    public void willCopyLduAcrossAsTeamType() {
        assertThat(TeamTransformer
            .teamOf(aTeam().toBuilder()
                .localDeliveryUnit(LocalDeliveryUnit.builder().code("LL")
                    .description("My Description").build())
                .build())
            .getTeamType()).isEqualTo(
            KeyValue.builder().code("LL").description("My Description").build());
    }

    @Test
    public void willStartDate() {
        assertThat(TeamTransformer
            .teamOf(aTeam().toBuilder()
                .startDate(LocalDate.now())
                .build())
            .getStartDate()).isEqualTo(LocalDate.now());

        assertThat(TeamTransformer
            .teamOf(aTeam().toBuilder()
                .build())
            .getStartDate()).isEqualTo(LocalDate.now());
    }

    @Test
    public void willEndDate() {
        assertThat(TeamTransformer
            .teamOf(aTeam().toBuilder()
                .endDate(LocalDate.now())
                .build())
            .getEndDate()).isEqualTo(LocalDate.now());

        assertThat(TeamTransformer
            .teamOf(aTeam().toBuilder()
                .build())
            .getEndDate()).isNull();
    }

    @Test
    public void willCopyBorough() {
        assertThat(TeamTransformer.teamOf(aTeam()
                .toBuilder()
                .district(District
                    .builder()
                    .borough(Borough
                        .builder()
                        .code("BB")
                        .description("My Description")
                        .build())
                    .build())
                .build())
                .getBorough()).isEqualTo(
                        KeyValue
                            .builder()
                            .code("BB")
                            .description("My Description")
                            .build());
    }
}