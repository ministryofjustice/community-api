package uk.gov.justice.digital.delius.service;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.CustodyNotFoundException;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document.DocumentType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAccessLimitations;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderAccessLimitationRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository.DuplicateOffenderException;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.ReleaseTransformer;

import java.util.Optional;

import static java.util.function.Predicate.not;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderService {

    private final OffenderRepository offenderRepository;
    private final OffenderAccessLimitationRepository offenderAccessLimitationRepository;
    private final ConvictionService convictionService;

    private final OffenderDocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByCrn(String crn) {

        return offenderRepository.findByCrn(crn).map(o -> {
                OffenderDocument document = documentRepository.findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(
                    o.getOffenderId(), DocumentType.PREVIOUS_CONVICTION
                );
                return OffenderTransformer.fullOffenderOf(o, document);
            }
        );
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByCrn(String crn) {

        return offenderRepository.findByCrn(crn).map(o -> {
                OffenderDocument document = documentRepository.findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(
                    o.getOffenderId(), DocumentType.PREVIOUS_CONVICTION
                );
                return OffenderTransformer.offenderSummaryOf(o, document);
            }
        );
    }

    @Transactional(readOnly = true)
    public Optional<OffenderAccessLimitations> getOffenderAccessLimitationsByCrn(String crn) {
        return offenderAccessLimitationRepository.findByCrn(crn);
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

    public Either<DuplicateOffenderException, Optional<Long>> singleOffenderIdOfNomsNumber(String nomsNumber) {
        return tryToGetSingleOffenderByNomsNumber(nomsNumber).fold(
            Either::left,
            offender -> Either.right(offender.map(Offender::getOffenderId))
        );
    }

    public Either<DuplicateOffenderException, Optional<Long>> mostLikelyOffenderIdOfNomsNumber(String nomsNumber) {
        return offenderRepository.findMostLikelyByNomsNumber(nomsNumber).fold(
            Either::left,
            offender -> Either.right(offender.map(Offender::getOffenderId))
        );
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetail>> getMostLikelyOffenderByNomsNumber(String nomsNumber) {
        return offenderRepository.findMostLikelyByNomsNumber(nomsNumber).fold(
            Either::left,
            offender -> Either.right(offender.map(this::fullOffenderWithPrecons))
        );
    }

    private OffenderDetail fullOffenderWithPrecons(Offender offender) {
        OffenderDocument document = documentRepository.findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(
            offender.getOffenderId(), DocumentType.PREVIOUS_CONVICTION
        );
        return OffenderTransformer.fullOffenderOf(offender, document);
    }

    private OffenderDetailSummary offenderSummaryWithPrecons(Offender offender) {
        OffenderDocument document = documentRepository.findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(
            offender.getOffenderId(), DocumentType.PREVIOUS_CONVICTION
        );
        return OffenderTransformer.offenderSummaryOf(offender, document);
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetailSummary>> getMostLikelyOffenderSummaryByNomsNumber(
        String nomsNumber
    ) {
        return offenderRepository.findMostLikelyByNomsNumber(nomsNumber).fold(
            Either::left,
            offender -> Either.right(offender.map(this::offenderSummaryWithPrecons))
        );
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetail>> getSingleOffenderByNomsNumber(String nomsNumber) {
        return tryToGetSingleOffenderByNomsNumber(nomsNumber).fold(
            Either::left,
            offender -> Either.right(offender.map(this::fullOffenderWithPrecons))
        );
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetailSummary>> getSingleOffenderSummaryByNomsNumber(
        String nomsNumber
    ) {
        return tryToGetSingleOffenderByNomsNumber(nomsNumber).fold(
            Either::left,
            offender -> Either.right(offender.map(this::offenderSummaryWithPrecons))
        );
    }

    public Either<DuplicateOffenderException, Optional<Offender>> tryToGetSingleOffenderByNomsNumber(String nomsNumber) {
        final var list = offenderRepository.findAllByNomsNumber(nomsNumber);
        switch (list.size()) {
            case 0:
                return Either.right(Optional.empty());
            case 1:
                return Either.right(Optional.of(list.get(0)));
            default:
                return Either.left(new DuplicateOffenderException(String.format(
                    "Expect a single offender with noms number %s but foud %d",
                    nomsNumber,
                    list
                        .size()
                )));
        }
    }

    @Transactional(readOnly = true)
    public OffenderLatestRecall getOffenderLatestRecall(Long offenderId) {
        final var actualCustodialEvent = convictionService.getActiveCustodialEvent(offenderId);
        final var custody = findCustodyOrThrow(actualCustodialEvent);
        return custody.findLatestRelease()
            .map(ReleaseTransformer::offenderLatestRecallOf)
            .orElse(OffenderLatestRecall.NO_RELEASE);
    }

    private Custody findCustodyOrThrow(Event activeCustodialEvent) {
        return Optional.ofNullable(activeCustodialEvent.getDisposal())
            .filter(not(Disposal::isSoftDeleted))
            .map(Disposal::getCustody)
            .filter(not(Custody::isSoftDeleted))
            .orElseThrow(() -> new CustodyNotFoundException(activeCustodialEvent));
    }
}
