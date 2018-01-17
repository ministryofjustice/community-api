package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.math.BigDecimal;
import java.util.List;
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
    public Optional<OffenderDetail> getOffenderByOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offenderTransformer::offenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByCrn(String crn) {

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(crn);

        return maybeOffender.map(offenderTransformer::offenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByNomsNumber(String nomsNumber) {

        Optional<Offender> maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);

        return maybeOffender.map(offenderTransformer::offenderOf);
    }

    public Optional<String> crnOf(Long offenderId) {
        return offenderRepository.findByOffenderId(offenderId).map(offender -> offender.getCrn());
    }

    public Optional<String> crnOf(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(offender -> offender.getCrn());
    }

    public List<BigDecimal> allOffenderIds(int pageSize, int page) {

        int lower = (page * pageSize) - pageSize + 1;
        int upper = page * pageSize;

        return offenderRepository.listOffenderIds(lower, upper);
    }

    public Long getOffenderCount() {
        return offenderRepository.count();
    }
}
