package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OffenderIdentifierService {
    private final boolean updateNomsNumberFeatureSwitch;

    public OffenderIdentifierService(
            @Value("${features.noms.update.noms.number}")
                    Boolean updateNomsNumberFeatureSwitch) {
        this.updateNomsNumberFeatureSwitch = updateNomsNumberFeatureSwitch;
        log.info("NOMIS update NOMS number feature is {}", this.updateNomsNumberFeatureSwitch ? "ON" : "OFF");
    }

}
