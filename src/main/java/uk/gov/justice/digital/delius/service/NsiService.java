package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.jpa.standard.repository.NsiRepository;
import uk.gov.justice.digital.delius.transformers.NsiTransformer;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NsiService {

    private final NsiRepository nsiRepository;

    private final ConvictionService convictionService;

    @Autowired
    public NsiService(final NsiRepository nsiRepository, final ConvictionService convictionService) {
        this.nsiRepository = nsiRepository;
        this.convictionService = convictionService;
    }

    @Transactional(readOnly = true)
    public Optional<NsiWrapper> getNsiByCodes(final Long offenderId, final Long eventId, final Collection<String> nsiCodes) {

        return convictionService.convictionFor(offenderId, eventId)
            .map(conv -> nsiRepository.findByEventIdAndOffenderId(eventId, offenderId))
            .map(nsis -> nsis.stream()
                .filter(e -> !e.getEvent().isSoftDeleted())
                .filter(e -> nsiCodes.contains(e.getNsiType().getCode()))
                .map(NsiTransformer::nsiOf)
                .collect(Collectors.toList()))
            .map(NsiWrapper::new);
    }

    public Optional<Nsi> getNsiById(Long nsiId) {
        return nsiRepository.findById(nsiId)
                .map(NsiTransformer::nsiOf);
    }

    @Transactional(readOnly = true)
    public NsiWrapper getNsiByCodesForOffenderActiveConvictions(final Long offenderId, final Collection<String> nsiCodes) {

        return new NsiWrapper(
                nsiRepository.findByOffenderIdForActiveEvents(offenderId)
                        .stream()
                        .filter(nsi -> !nsi.getSoftDeleted())
                        .filter(nsi -> nsiCodes.contains(nsi.getNsiType().getCode()))
                        .map(NsiTransformer::nsiOf)
                        .collect(Collectors.toList()));
    }

}
