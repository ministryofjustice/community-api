package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.AllTeam;
import uk.gov.justice.digital.delius.data.api.Institution;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.ExternalProvider;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;
import uk.gov.justice.digital.delius.jpa.standard.entity.Organisation;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProbationAreaTransformer {

    public List<ProbationArea> probationAreasOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea> probationAreas) {
        return probationAreas.stream().map(this::probationAreaOf).collect(Collectors.toList());
    }

    public ProbationArea probationAreaOf(uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea probationArea) {
        return ProbationArea.builder()
                .code(probationArea.getCode())
                .description(probationArea.getDescription())
                .organisation(organisationOf(probationArea.getOrganisation()))
                .institution(institutionOf(probationArea.getInstitution()))
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
                .isPrivate(team.getPrivateFlag().equals(1L))
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
        Optional<Borough> maybeBorough = Optional.ofNullable(district).map(d -> d.getBorough());
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

    private Institution institutionOf(RInstitution institution) {
        return Optional.ofNullable(institution).map(inst -> Institution.builder()
                .code(inst.getCode())
                .description(inst.getDescription())
                .isEstablishment(ynToBoolean(inst.getEstablishment()))
                .establishmentType(establishmentTypeOf(inst.getEstablishmentType()))
                .institutionId(inst.getInstitutionId())
                .institutionName(inst.getInstitutionName())
                .isPrivate(Optional.ofNullable(inst.getPrivateFlag()).map(pf -> pf.equals(1L)).orElse(null))
                .build()).orElse(null);
    }

    private KeyValue establishmentTypeOf(StandardReference establishmentType) {
        return Optional.ofNullable(establishmentType).map(et -> KeyValue.builder()
                .code(et.getCodeValue())
                .description(et.getCodeDescription())
                .build())
                .orElse(null);
    }

    public static Boolean ynToBoolean(String yn) {
        return Optional.ofNullable(yn).map("Y"::equalsIgnoreCase).orElse(null);
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
