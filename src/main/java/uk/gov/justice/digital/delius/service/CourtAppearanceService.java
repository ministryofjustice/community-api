package uk.gov.justice.digital.delius.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasicWrapper;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceMinimal;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceBasicTransformer;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceMinimalTransformer;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;

import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.OffenceIdTransformer.additionalOffenceIdOf;
import static uk.gov.justice.digital.delius.transformers.OffenceIdTransformer.mainOffenceIdOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class CourtAppearanceService {

    private final CourtAppearanceRepository courtAppearanceRepository;
    private final ConvictionService convictionService;

    @Autowired
    public CourtAppearanceService(CourtAppearanceRepository courtAppearanceRepository, ConvictionService convictionService) {

        this.courtAppearanceRepository = courtAppearanceRepository;
        this.convictionService = convictionService;
    }

    public List<CourtAppearance> courtAppearancesFor(Long offenderId) {

        List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance> courtAppearances = courtAppearanceRepository.findByOffenderId(offenderId);
        return buildCourtAppearanceList(courtAppearances);
    }

    public Optional<CourtAppearanceBasicWrapper> courtAppearancesFor(Long offenderId, Long eventId) {

       return convictionService.convictionFor(offenderId, eventId)
            .map(conviction -> courtAppearanceRepository.findByOffenderIdAndEventId(offenderId, eventId))
            .map(appearances -> appearances.stream()
                .filter(courtAppearance -> !convertToBoolean(courtAppearance.getSoftDeleted()))
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate, Comparator.reverseOrder()))
                .map(CourtAppearanceBasicTransformer::courtAppearanceOf)
                .collect(Collectors.toList()))
            .map(CourtAppearanceBasicWrapper::new);
    }

    public List<CourtAppearanceMinimal> courtAppearances(LocalDate fromDate) {

        var courtAppearances = courtAppearanceRepository.findByAppearanceDateGreaterThanEqualAndSoftDeletedNot(fromDate.atStartOfDay(), 1L);
        return courtAppearances
                .stream()
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate))
                .map(CourtAppearanceMinimalTransformer::courtAppearanceOf)
                .toList();
    }

    @NotNull
    private List<CourtAppearance> buildCourtAppearanceList(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance> courtAppearances) {
        return courtAppearances
            .stream()
            .filter(courtAppearance -> !convertToBoolean(courtAppearance.getSoftDeleted()))
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate).reversed())
            .map(courtAppearance -> CourtAppearanceTransformer
                    .courtAppearanceOf(courtAppearance).toBuilder()
                .offenceIds(combined(mainOffenceIds(courtAppearance), additionalOffenceIds(courtAppearance)))
                .build())
            .collect(toList());
    }

    private List<String> combined(List<String> one, List<String> two) {
        List<String> combined = new ArrayList<>();
        combined.addAll(one);
        combined.addAll(two);
        return combined;
    }

    private List<String> mainOffenceIds(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return Optional.ofNullable(courtAppearance.getEvent().getMainOffence())
                .map(mainOffence -> List.of(mainOffenceIdOf(mainOffence.getMainOffenceId())))
                .orElse(List.of());
    }

    private List<String> additionalOffenceIds(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return courtAppearance.getEvent().getAdditionalOffences()
                .stream()
                .map(additionalOffence -> additionalOffenceIdOf(additionalOffence.getAdditionalOffenceId()))
                .toList();
    }
}
