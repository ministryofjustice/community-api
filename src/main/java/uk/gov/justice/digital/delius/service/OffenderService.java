package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.CustodyNotFoundException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderService {

    private final OffenderRepository offenderRepository;
    private final OffenderTransformer offenderTransformer;
    private final ConvictionService convictionService;

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByCrn(String crn) {

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(crn);

        return maybeOffender.map(offenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByNomsNumber(String nomsNumber) {

        Optional<Offender> maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);

        return maybeOffender.map(offenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offenderTransformer::offenderSummaryOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByCrn(String crn) {

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(crn);

        return maybeOffender.map(offenderTransformer::offenderSummaryOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByNomsNumber(String nomsNumber) {

        Optional<Offender> maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);

        return maybeOffender.map(offenderTransformer::offenderSummaryOf);
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
        } else if (offenderIds.contains(null)) {
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

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> offenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForCrn(String crn) {
        return offenderRepository.findByCrn(crn).map(
                offender -> offenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<ResponsibleOfficer>> getResponsibleOfficersForNomsNumber(String nomsNumber, boolean current) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> offenderTransformer.responsibleOfficersOf(offender, current));

    }

    // TODO DT-337 Flesh out this stub
    @Transactional(readOnly = true)
    public OffenderLatestRecall getOffenderLatestRecall(Long offenderId) {
        offenderRepository.findByOffenderId(offenderId)
                .map(offender -> convictionService.getActiveCustodialEvent(offender.getOffenderId()))
                .map(activeCustodialEvent -> {
                    return Optional.ofNullable(activeCustodialEvent.getDisposal())
                            .map(Disposal::getCustody)
                            .orElseThrow(() -> new CustodyNotFoundException(activeCustodialEvent));
                })
                .orElseThrow(() -> new NotFoundException("Offender not found"));
        return null;
    }
}
