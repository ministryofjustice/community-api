package uk.gov.justice.digital.delius.service;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.CustodyNotFoundException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderIdentifiers;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.data.api.PrimaryIdentifiers;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.data.filters.OffenderFilter;
import uk.gov.justice.digital.delius.jpa.filters.OffenderFilterTransformer;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderPrimaryIdentifiersRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository.DuplicateOffenderException;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.ReleaseTransformer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderService {

    private final OffenderRepository offenderRepository;
    private final OffenderPrimaryIdentifiersRepository offenderPrimaryIdentifiersRepository;
    private final ConvictionService convictionService;
    private final StandardReferenceRepository standardReferenceRepository;

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(OffenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByCrn(String crn) {

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(crn);

        return maybeOffender.map(OffenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByNomsNumber(String nomsNumber) {

        Optional<Offender> maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);

        return maybeOffender.map(OffenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(OffenderTransformer::offenderSummaryOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByCrn(String crn) {

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(crn);

        return maybeOffender.map(OffenderTransformer::offenderSummaryOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByNomsNumber(String nomsNumber) {

        Optional<Offender> maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);

        return maybeOffender.map(OffenderTransformer::offenderSummaryOf);
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

    public Either<DuplicateOffenderException, Optional<Long>> mostLikelyOffenderIdOfNomsNumber(String nomsNumber) {
        return offenderRepository.findMostLikelyByNomsNumber(nomsNumber).fold(Either::left,
                offender -> Either.right(offender.map(Offender::getOffenderId)));
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
                offender -> OffenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> OffenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForCrn(String crn) {
        return offenderRepository.findByCrn(crn).map(
                offender -> OffenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<ResponsibleOfficer>> getResponsibleOfficersForNomsNumber(String nomsNumber, boolean current) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> OffenderTransformer.responsibleOfficersOf(offender, current));

    }

    @Transactional(readOnly = true)
    public OffenderLatestRecall getOffenderLatestRecall(Long offenderId) {
        final var actualCustodialEvent = convictionService.getActiveCustodialEvent(offenderId);
        final var custody = findCustodyOrThrow(actualCustodialEvent);
        return custody.findLatestRelease()
                .map(ReleaseTransformer::offenderLatestRecallOf)
                .orElse(OffenderLatestRecall.NO_RELEASE);
    }

    @Transactional(readOnly = true)
    public OffenderIdentifiers getOffenderIdentifiers(Long offenderId) {
        var offender = offenderRepository.findByOffenderId(offenderId)
                .orElseThrow(() -> new NotFoundException("Offender not found"));

        return OffenderIdentifiers
                .builder()
                .primaryIdentifiers(OffenderTransformer.idsOf(offender))
                .additionalIdentifiers(OffenderTransformer.additionalIdentifiersOf(offender.getAdditionalIdentifiers()))
                .offenderId(offender.getOffenderId())
                .build();
    }

    private Custody findCustodyOrThrow(Event activeCustodialEvent) {
        return Optional.ofNullable(activeCustodialEvent.getDisposal())
                .filter(not(Disposal::isSoftDeleted))
                .map(Disposal::getCustody)
                .filter(not(Custody::isSoftDeleted))
                .orElseThrow(() -> new CustodyNotFoundException(activeCustodialEvent));
    }

    @NationalUserOverride
    @Transactional(readOnly = true)
    public Page<PrimaryIdentifiers> getAllPrimaryIdentifiers(OffenderFilter filter, Pageable pageable) {
        return offenderPrimaryIdentifiersRepository
                .findAll(OffenderFilterTransformer.fromFilter(filter), pageable)
                .map(offender -> PrimaryIdentifiers
                        .builder()
                        .crn(offender.getCrn())
                        .offenderId(offender.getOffenderId())
                        .build());
    }

    @Transactional
    public OffenderDetail updateTier(String crn, String tier) {
       return offenderRepository.findByCrn(crn).map(offender -> standardReferenceRepository.findByCodeAndCodeSetName(tier, "TIER").map(t -> {
           offender.setCurrentTier(t);
           return OffenderTransformer.fullOffenderOf(offenderRepository.save(offender));
       })
           .orElseThrow(() -> new NotFoundException(String.format("Tier %s not found",tier))))
           .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN %s not found",crn)));
    }
}
