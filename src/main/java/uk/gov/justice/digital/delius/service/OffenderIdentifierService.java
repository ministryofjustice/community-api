package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

@Service
@Slf4j
public class OffenderIdentifierService {
    private final boolean updateNomsNumberFeatureSwitch;
    private final OffenderTransformer offenderTransformer;
    private final OffenderRepository offenderRepository;

    public OffenderIdentifierService(
            @Value("${features.noms.update.noms.number}") Boolean updateNomsNumberFeatureSwitch,
            OffenderTransformer offenderTransformer,
            OffenderRepository offenderRepository) {
        this.updateNomsNumberFeatureSwitch = updateNomsNumberFeatureSwitch;
        this.offenderTransformer = offenderTransformer;
        this.offenderRepository = offenderRepository;
        log.info("NOMIS update NOMS number feature is {}", this.updateNomsNumberFeatureSwitch ? "ON" : "OFF");
    }

    @Transactional
    public IDs updateNomsNumber(String crn, UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        final var offender = offenderRepository.findByCrn(crn).orElseThrow(() -> new NotFoundException(String
                .format("Offender with crn %s not found ", crn)));
        if (updateNomsNumberFeatureSwitch) {
            // TODO replace with real update
            return offenderTransformer.idsOf(offender).toBuilder().nomsNumber(updateOffenderNomsNumber.getNomsNumber()).build();
        }
        log.warn("Update NOMS number will be ignored, this feature is switched off ");
        return offenderTransformer.idsOf(offender);
    }
}
