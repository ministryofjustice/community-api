package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalIdentifier;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.util.Optional;

@Service
@Slf4j
public class OffenderIdentifierService {
    private final boolean updateNomsNumberFeatureSwitch;
    private final OffenderTransformer offenderTransformer;
    private final OffenderRepository offenderRepository;
    private final SpgNotificationService spgNotificationService;
    private final ReferenceDataService referenceDataService;

    public OffenderIdentifierService(
            @Value("${features.noms.update.noms.number}") Boolean updateNomsNumberFeatureSwitch,
            OffenderTransformer offenderTransformer,
            OffenderRepository offenderRepository,
            SpgNotificationService spgNotificationService,
            ReferenceDataService referenceDataService) {
        this.updateNomsNumberFeatureSwitch = updateNomsNumberFeatureSwitch;
        this.offenderTransformer = offenderTransformer;
        this.offenderRepository = offenderRepository;
        this.spgNotificationService = spgNotificationService;
        this.referenceDataService = referenceDataService;
        log.info("NOMIS update NOMS number feature is {}", this.updateNomsNumberFeatureSwitch ? "ON" : "OFF");
    }

    @Transactional
    public IDs updateNomsNumber(String crn, UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        final var offender = offenderRepository.findByCrn(crn).orElseThrow(() -> new NotFoundException(String
                .format("Offender with crn %s not found ", crn)));
        if (updateNomsNumberFeatureSwitch) {
            if (!updateOffenderNomsNumber.getNomsNumber().equals(offender.getNomsNumber())) {
                doUpdateNomsNumber(updateOffenderNomsNumber.getNomsNumber(), offender);
            } else {
                log.info("No need to Update NOMS number since it is already set");
            }
        } else {
            log.warn("Update NOMS number will be ignored, this feature is switched off ");
        }
        return offenderTransformer.idsOf(offender);
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
            spgNotificationService.notifyUpdateOfOffender(duplicateOffender);
            spgNotificationService.notifyInsertOfOffenderAdditionalIdentifier(duplicateOffender, additionalIdentifier);
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
                    spgNotificationService
                            .notifyInsertOfOffenderAdditionalIdentifier(offender, additionalIdentifier);
                }

        );
        spgNotificationService.notifyUpdateOfOffender(offender);
    }
}
