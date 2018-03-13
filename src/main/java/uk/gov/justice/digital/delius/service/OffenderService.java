package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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
        return offenderRepository.findByOffenderId(offenderId).map(Offender::getCrn);
    }

    public Optional<String> crnOf(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(Offender::getCrn);
    }

    public Optional<Long> offenderIdOfCrn(String crn) {
        return offenderRepository.findByCrn(crn).map(Offender::getOffenderId);
    }

    public Optional<Long> offenderIdOfNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(Offender::getOffenderId);
    }

    public List<BigDecimal> allOffenderIds(int pageSize, int page) {

        int lower = (page * pageSize) - pageSize + 1;
        int upper = page * pageSize;

        List<BigDecimal> offenderIds = offenderRepository.listOffenderIds(lower, upper);

        if (offenderIds == null) {
            log.error("Call to offenderRepository.listOffenderIds {}, {} returned a null list", pageSize, page);
        }

        if (offenderIds.contains(null)) {
            log.error("Call to offenderRepository.listOffenderIds {}, {} returned a list containing null", pageSize, page);
        }

        return offenderIds;
    }

    public Long getOffenderCount() {
        return offenderRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForOffenderId(Long offenderId) {
        return offenderRepository.findByOffenderId(offenderId).map(
                offender -> offenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

}
