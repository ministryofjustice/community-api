package uk.gov.justice.digital.delius.service;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.jpa.standard.repository.NsiRepository;
import uk.gov.justice.digital.delius.transformers.NsiTransformer;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
@Slf4j
public class NsiService {
    private final NsiRepository nsiRepository;

    private final NsiTransformer nsiTransformer;
    private final ConvictionService convictionService;

    @Autowired
    public NsiService(final NsiRepository nsiRepository, final NsiTransformer nsiTransformer, final ConvictionService convictionService) {
        this.nsiRepository = nsiRepository;
        this.nsiTransformer = nsiTransformer;
        this.convictionService = convictionService;
    }

    @Transactional(readOnly = true)
    public Optional<NsiWrapper> getNsiByCodes(final Long offenderId, final Long eventId, final Collection<String> nsiCodes) {

        return convictionService.convictionFor(offenderId, eventId)
            .map(conv -> nsiRepository.findByEventIdAndOffenderId(eventId, offenderId))
            .map(nsis -> nsis.stream()
                .filter(e -> !convertToBoolean(e.getEvent().getSoftDeleted()))
                .filter(e -> nsiCodes.contains(e.getNsiType().getCode()))
                .map(nsiTransformer::nsiOf)
                .collect(Collectors.toList()))
            .map(NsiWrapper::new);
    }

    public Optional<Nsi> getNsiById(Long nsiId) {
        var nsi = nsiRepository.getByNsiId(nsiId);
        return Optional.ofNullable(nsiTransformer.nsiOf(nsi));
    }
}
