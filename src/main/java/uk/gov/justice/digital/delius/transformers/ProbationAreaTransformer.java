package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.AllTeam;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class ProbationAreaTransformer {
    private final InstitutionTransformer institutionTransformer;

    public ProbationAreaTransformer(InstitutionTransformer institutionTransformer) {
        this.institutionTransformer = institutionTransformer;
    }

    public ProbationAreaTransformer() {
        this.institutionTransformer = new InstitutionTransformer();
    }

    public List<ProbationArea> probationAreasOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea> probationAreas) {
        return probationAreas.stream().map(this::probationAreaOf).collect(Collectors.toList());
    }

    public ProbationArea probationAreaOf(uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea probationArea) {
        return ProbationArea.builder()
                .code(probationArea.getCode())
                .description(probationArea.getDescription())
                .organisation(organisationOf(probationArea.getOrganisation()))
                .institution(institutionTransformer.institutionOf(probationArea.getInstitution()))
                .probationAreaId(probationArea.getProbationAreaId())
                .teams(teamsOf(probationArea))
                .build();
    }

    private List<AllTeam> teamsOf(uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea probationArea) {
        List<AllTeam> teams = probationArea.getTeams().stream().map(team -> AllTeam.builder()
                .code(team.getCode())
                .description(team.getDescription())
                .district(districtOf(team.getDistrict()))
                .borough(boroughOf(team.getDistrict()))
                .localDeliveryUnit(localDeliveryUnitOf(team.getLocalDeliveryUnit()))
                .isPrivate(zeroOneToBoolean(team.getPrivateFlag()))
                .scProvider(scProviderOf(team))
                .teamId(team.getTeamId())
                .build()).collect(Collectors.toList());

        List<AllTeam> providerTeams = probationArea.getProviderTeams().stream().map(providerTeam -> AllTeam.builder()
                .providerTeamId(providerTeam.getProviderTeamId())
                .code(providerTeam.getCode())
                .name(providerTeam.getName())
                .externalProvider(externalProviderOf(providerTeam.getExternalProvider()))
                .build()).collect(Collectors.toList());

        return Stream.concat(teams.stream(), providerTeams.stream()).collect(Collectors.toList());
    }

    private KeyValue externalProviderOf(ExternalProvider externalProvider) {
        return Optional.ofNullable(externalProvider)
                .map(ep -> KeyValue.builder()
                        .code(ep.getCode())
                        .description(ep.getDescription())
                        .build())
                .orElse(null);
    }

    private KeyValue scProviderOf(Team team) {
        return Optional.ofNullable(team)
                .map(t -> KeyValue.builder()
                        .code(t.getCode())
                        .description(t.getDescription())
                        .build())
                .orElse(null);
    }

    private KeyValue localDeliveryUnitOf(LocalDeliveryUnit localDeliveryUnit) {
        return Optional.ofNullable(localDeliveryUnit)
                .map(ldu -> KeyValue.builder()
                        .code(ldu.getCode())
                        .description(ldu.getDescription())
                        .build())
                .orElse(null);
    }

    private KeyValue boroughOf(District district) {
        Optional<Borough> maybeBorough = Optional.ofNullable(district).map(District::getBorough);
        return maybeBorough
                .map(b -> KeyValue.builder()
                        .code(b.getCode())
                        .description(b.getDescription())
                        .build())
                .orElse(null);
    }

    private KeyValue districtOf(District district) {
        return Optional.ofNullable(district)
                .map(d -> KeyValue.builder()
                        .code(d.getCode())
                        .description(d.getDescription())
                        .build())
                .orElse(null);
    }

    private KeyValue organisationOf(Organisation organisation) {
        return Optional.ofNullable(organisation)
                .map(org -> KeyValue.builder()
                        .code(org.getCode())
                        .description(org.getDescription())
                        .build())
                .orElse(null);
    }
}
