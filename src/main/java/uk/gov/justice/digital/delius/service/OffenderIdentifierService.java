package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalIdentifier;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OffenderIdentifierService {
    private final boolean updateNomsNumberFeatureSwitch;
    private final OffenderRepository offenderRepository;
    private final ReferenceDataService referenceDataService;

    public OffenderIdentifierService(
            @Value("${features.noms.update.noms.number}") Boolean updateNomsNumberFeatureSwitch,
            OffenderRepository offenderRepository,
            ReferenceDataService referenceDataService) {
        this.updateNomsNumberFeatureSwitch = updateNomsNumberFeatureSwitch;
        this.offenderRepository = offenderRepository;
        this.referenceDataService = referenceDataService;
        log.debug("NOMIS update NOMS number feature is {}", this.updateNomsNumberFeatureSwitch ? "ON" : "OFF");
    }

    @Transactional
    public IDs updateNomsNumber(String crn, UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        final var offender = offenderRepository.findByCrn(crn).orElseThrow(() -> new NotFoundException(String
                .format("Offender with crn %s not found ", crn)));
        if (updateNomsNumberFeatureSwitch && !updateOffenderNomsNumber.getNomsNumber().equals(offender.getNomsNumber())) {
                doUpdateNomsNumber(updateOffenderNomsNumber.getNomsNumber(), offender);
        } else {
            log.warn("Update NOMS number will be ignored, this feature is switched off ");
        }
        return OffenderTransformer.idsOf(offender);
    }
    @Transactional
    public List<IDs> replaceNomsNumber(String oldNomsNumber, UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        final var existingOffendersAlreadyWithNomsNumber = offenderRepository.findAllByNomsNumber(updateOffenderNomsNumber.getNomsNumber());

        if (!existingOffendersAlreadyWithNomsNumber.isEmpty()) {
            throw new ConflictingRequestException(String.format("NOMS number %s is already assigned to %s", updateOffenderNomsNumber.getNomsNumber(), existingOffendersAlreadyWithNomsNumber.stream().map(Offender::getCrn).collect(Collectors.joining(","))));
        }

        final var offenders = offenderRepository.findAllByNomsNumber(oldNomsNumber);

        if (offenders.isEmpty()) {
            throw new NotFoundException(String.format("Offender with noms number %s not found ", oldNomsNumber));
        }
        if (updateNomsNumberFeatureSwitch) {
            if (offenders.size() > 1) {
                log.warn("Multiple offenders found with the same NOMS number {}. Updating all to new number {}", oldNomsNumber, updateOffenderNomsNumber.getNomsNumber());
            }
            offenders.forEach(offender -> doUpdateNomsNumber(updateOffenderNomsNumber.getNomsNumber(), offender));
        } else {
            log.warn("Update NOMS number will be ignored, this feature is switched off ");
        }
        return offenders.stream().map(OffenderTransformer::idsOf).collect(Collectors.toList());
    }
    private void doUpdateNomsNumber(String nomsNumber, Offender offender) {
        offenderRepository.findAllByNomsNumber(nomsNumber).forEach(duplicateOffender -> {
            duplicateOffender.setNomsNumber(null);
            final var additionalIdentifier = AdditionalIdentifier
                    .builder()
                    .identifier(nomsNumber)
                    .offender(offender)
                    .identifierName(referenceDataService.duplicateNomsNumberAdditionalIdentifier())
                    .build();
            duplicateOffender.getAdditionalIdentifiers().add(additionalIdentifier);
            // required to force order of updates that could break NOMS_NUMBER unique constraint
            offenderRepository.flush();
        });

        var maybeExistingNomsNumber = Optional.ofNullable(offender.getNomsNumber());
        offender.setNomsNumber(nomsNumber);
        maybeExistingNomsNumber.ifPresent(
                existingNomsNumber -> {
                    final var additionalIdentifier = AdditionalIdentifier
                            .builder()
                            .identifier(existingNomsNumber)
                            .offender(offender)
                            .identifierName(referenceDataService.formerNomsNumberAdditionalIdentifier())
                            .build();
                    offender.getAdditionalIdentifiers().add(additionalIdentifier);
                    // force additional identifier to generate ID
                    offenderRepository.flush();
                }

        );
    }
}
