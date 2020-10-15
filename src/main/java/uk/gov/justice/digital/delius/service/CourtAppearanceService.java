package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasic;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasicWrapper;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceBasicTransformer;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<CourtAppearanceBasic> courtAppearances(LocalDate fromDate) {

        var courtAppearances = courtAppearanceRepository.findByAppearanceDateGreaterThanEqual(fromDate.atStartOfDay());
        return courtAppearances
                .stream()
                .filter(courtAppearance -> !convertToBoolean(courtAppearance.getSoftDeleted()))
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate))
                .map(CourtAppearanceBasicTransformer::courtAppearanceOf)
                .collect(toList());
    }

    @NotNull
    private List<CourtAppearance> buildCourtAppearanceList(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance> courtAppearances) {
        return courtAppearances
            .stream()
            .filter(courtAppearance -> !convertToBoolean(courtAppearance.getSoftDeleted()))
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate).reversed())
            .map(courtAppearance -> CourtAppearanceTransformer
                    .courtAppearanceOf(courtAppearance).toBuilder()
                .offenceIds(
                    ImmutableList.<String>builder()
                    .addAll(mainOffenceIds(courtAppearance))
                    .addAll(additionalOffenceIds(courtAppearance))
                    .build()
                ).build())
            .collect(toList());
    }

    private List<String> mainOffenceIds(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return Optional.ofNullable(courtAppearance.getEvent().getMainOffence())
                .map(mainOffence -> ImmutableList.of(mainOffenceIdOf(mainOffence.getMainOffenceId())))
                .orElse(ImmutableList.of());
    }

    private List<String> additionalOffenceIds(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return courtAppearance.getEvent().getAdditionalOffences()
                .stream()
                .map(additionalOffence -> additionalOffenceIdOf(additionalOffence.getAdditionalOffenceId()))
                .collect(toList());
    }
}
