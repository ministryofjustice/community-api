package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.OffenderDelta;

import java.util.Optional;

@Service
@Slf4j
public class OffenderUpdatesService {

    private final OffenderDeltaService offenderDeltaService;
    @SuppressWarnings("FieldMayBeFinal")
    private int retries = 10;

    public OffenderUpdatesService(OffenderDeltaService offenderDeltaService) {
        this.offenderDeltaService = offenderDeltaService;
    }

    public Optional<OffenderDelta> getNextUpdate() {
        for(int i=0; i<retries; i++) {
            try {
                return offenderDeltaService.lockNext();
            } catch(ConcurrencyFailureException ex) {
                log.warn("Received ConcurrencyFailureException while trying to getNextUpdate");
            }
        }
        throw new OffenderDeltaLockedException();
    }

}
