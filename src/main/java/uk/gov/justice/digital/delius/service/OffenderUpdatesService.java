package uk.gov.justice.digital.delius.service;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.OffenderDelta;

import java.util.Optional;

@Service
public class OffenderUpdatesService {

    private final OffenderDeltaService offenderDeltaService;
    private int retries = 10;

    public OffenderUpdatesService(OffenderDeltaService offenderDeltaService) {
        this.offenderDeltaService = offenderDeltaService;
    }

    public Optional<OffenderDelta> getNextUpdate() {
        for(int i=0; i<retries; i++) {
            try {
                return offenderDeltaService.lockNext();
            } catch(ConcurrencyFailureException ex) {
                // ignoring expected locking failures
            }
        }
        return Optional.empty();
    }

}
