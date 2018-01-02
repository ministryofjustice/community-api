package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.util.Optional;

@Service
public class OffenderService {

    private final OffenderRepository offenderRepository;
    private final OffenderTransformer offenderTransformer;

    @Autowired
    public OffenderService(OffenderRepository offenderRepository, OffenderTransformer offenderTransformer) {
        this.offenderRepository = offenderRepository;
        this.offenderTransformer = offenderTransformer;
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffender(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offenderTransformer::offenderOf);
    }

}
