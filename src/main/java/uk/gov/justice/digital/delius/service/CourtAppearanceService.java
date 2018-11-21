package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.repository.AdditionalOffenceRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.OffenceIdTransformer.additionalOffenceIdOf;
import static uk.gov.justice.digital.delius.transformers.OffenceIdTransformer.mainOffenceIdOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class CourtAppearanceService {

    private final CourtAppearanceRepository courtAppearanceRepository;
    private final CourtAppearanceTransformer courtAppearanceTransformer;
    private final MainOffenceRepository mainOffenceRepository;
    private final AdditionalOffenceRepository additionalOffenceRepository;

    @Autowired
    public CourtAppearanceService(CourtAppearanceRepository courtAppearanceRepository,
                                  CourtAppearanceTransformer courtAppearanceTransformer,
                                  MainOffenceRepository mainOffenceRepository,
                                  AdditionalOffenceRepository additionalOffenceRepository) {

        this.courtAppearanceRepository = courtAppearanceRepository;
        this.courtAppearanceTransformer = courtAppearanceTransformer;
        this.mainOffenceRepository = mainOffenceRepository;
        this.additionalOffenceRepository = additionalOffenceRepository;
    }

    public List<CourtAppearance> courtAppearancesFor(Long offenderId) {

        List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance> courtAppearances = courtAppearanceRepository.findByOffenderId(offenderId);
        return courtAppearances
            .stream()
            .filter(courtAppearance -> !convertToBoolean(courtAppearance.getSoftDeleted()))
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate).reversed())
            .map(courtAppearance -> courtAppearanceTransformer.courtAppearanceOf(courtAppearance).toBuilder()
                .offenceIds(
                    ImmutableList.<String>builder()
                    .addAll(mainOffenceIds(courtAppearance))
                    .addAll(additionalOffenceIds(courtAppearance))
                    .build()
                ).build())
            .collect(toList());
    }

    Optional<CourtAppearance> findCourtAppearanceByEventId(Long convictionId) {
        Optional<uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance> maybeCourtAppearance =
            courtAppearanceRepository.findByEventId(convictionId);

        return maybeCourtAppearance
            .filter(courtAppearance -> !convertToBoolean(courtAppearance.getSoftDeleted()))
            .map(courtAppearance -> courtAppearanceTransformer.courtAppearanceOf(courtAppearance).toBuilder()
                .offenceIds(
                    ImmutableList.<String>builder()
                        .addAll(mainOffenceIds(courtAppearance))
                        .addAll(additionalOffenceIds(courtAppearance))
                        .build()
                ).build());
    }

    private List<String> mainOffenceIds(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return mainOffenceRepository.findByEventId(courtAppearance.getEventId())
            .stream()
            .map(mainOffence -> mainOffenceIdOf(mainOffence.getMainOffenceId()))
            .collect(toList());
    }

    private List<String> additionalOffenceIds(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return additionalOffenceRepository.findByEventId(courtAppearance.getEventId())
            .stream()
            .map(additionalOffence -> additionalOffenceIdOf(additionalOffence.getAdditionalOffenceId()))
            .collect(toList());
    }
}
