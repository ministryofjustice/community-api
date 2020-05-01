package uk.gov.justice.digital.delius.service;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.jpa.standard.repository.NsiRepository;
import uk.gov.justice.digital.delius.transformers.NsiTransformer;

@Service
@Slf4j
public class NsiService {
    private final NsiRepository nsiRepository;

    private final NsiTransformer nsiTransformer;

    @Autowired
    public NsiService(final NsiRepository nsiRepository, final NsiTransformer nsiTransformer) {
        this.nsiRepository = nsiRepository;
        this.nsiTransformer = nsiTransformer;
    }

    @Transactional(readOnly = true)
    public Optional<NsiWrapper> getNsiByCodes(final Long offenderId, final Long eventId, final Collection<String> nsiCodes) {

        val nsi = nsiRepository.findByEventIdAndOffenderId(eventId, offenderId);
        if (nsi.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new NsiWrapper(nsi.stream()
            .filter(e -> !convertToBoolean(e.getEvent().getSoftDeleted()))
            .filter(e -> nsiCodes.contains(e.getNsiType().getCode()))
            .map(nsiTransformer::nsiOf)
            .collect(toList())));
    }


}
