package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProbationAreaTransformerTest {

    @Mock
    private Team team;
    @Mock
    private ProviderTeam providerTeam;

    private ProbationArea probationArea;

    @BeforeEach
    public void setUp() {
        probationArea = ProbationArea.builder()
                .teams(Collections.singletonList(team))
                .providerTeams(Collections.singletonList(providerTeam))
                .build();
    }

    @Test
    public void whenIncludeTeamsNotSpecifiedThenMapTeams() {
        when(team.getCode()).thenReturn("team");
        when(providerTeam.getCode()).thenReturn("providerteam");

        var result = ProbationAreaTransformer.probationAreaOf(probationArea);
        assertThat(result.getTeams()).hasSize(2);
        assertThat(result.getTeams().get(0).getCode()).isEqualTo("team");
        assertThat(result.getTeams().get(1).getCode()).isEqualTo("providerteam");
    }

    @Test
    public void whenIncludeTeamsIsTrueThenMapTeams() {
        when(team.getCode()).thenReturn("team");
        when(providerTeam.getCode()).thenReturn("providerteam");

        var result = ProbationAreaTransformer.probationAreaOf(probationArea, true);
        assertThat(result.getTeams()).hasSize(2);
        assertThat(result.getTeams().get(0).getCode()).isEqualTo("team");
        assertThat(result.getTeams().get(1).getCode()).isEqualTo("providerteam");
    }

    @Test
    public void whenIncludeTeamsIsFalseThenDontMapTeams() {
        var result = ProbationAreaTransformer.probationAreaOf(probationArea, false);
        assertThat(result.getTeams()).isNull();
    }

}