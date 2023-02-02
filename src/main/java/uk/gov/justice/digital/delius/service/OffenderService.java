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
import uk.gov.justice.digital.delius.data.api.PersonalContact;
import uk.gov.justice.digital.delius.data.api.PrimaryIdentifiers;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.data.api.StaffCaseloadEntry;
import uk.gov.justice.digital.delius.data.filters.OffenderFilter;
import uk.gov.justice.digital.delius.jpa.filters.OffenderFilterTransformer;
import uk.gov.justice.digital.delius.jpa.national.repository.DocumentRepository;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document.DocumentType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderDocumentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderPrimaryIdentifiersRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository.DuplicateOffenderException;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.PersonalContactTransformer;
import uk.gov.justice.digital.delius.transformers.ReleaseTransformer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderService {

    private final OffenderRepository offenderRepository;
    private final OffenderPrimaryIdentifiersRepository offenderPrimaryIdentifiersRepository;
    private final ConvictionService convictionService;

    private final OffenderDocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByOffenderId(Long offenderId) {

        return offenderRepository.findByOffenderId(offenderId).map(o -> {
                OffenderDocument document = documentRepository.findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(
                    o.getOffenderId(), DocumentType.PREVIOUS_CONVICTION
                );
                return OffenderTransformer.fullOffenderOf(o, document);
            }
        );
    }

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
        return tryToGetSingleOffenderByNomsNumber(nomsNumber).fold(Either::left,
            offender -> Either.right(offender.map(Offender::getOffenderId)));
    }

    public Either<DuplicateOffenderException, Optional<Long>> mostLikelyOffenderIdOfNomsNumber(String nomsNumber) {
        return offenderRepository.findMostLikelyByNomsNumber(nomsNumber).fold(Either::left,
            offender -> Either.right(offender.map(Offender::getOffenderId)));
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetail>> getMostLikelyOffenderByNomsNumber(String nomsNumber) {
        return offenderRepository.findMostLikelyByNomsNumber(nomsNumber).fold(Either::left,
            offender -> Either.right(offender.map(this::fullOffenderWithPrecons)));
    }

    private OffenderDetail fullOffenderWithPrecons(Offender offender){
        OffenderDocument document = documentRepository.findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(
            offender.getOffenderId(), DocumentType.PREVIOUS_CONVICTION
        );
        return OffenderTransformer.fullOffenderOf(offender, document);
    }

    private OffenderDetailSummary offenderSummaryWithPrecons(Offender offender){
        OffenderDocument document = documentRepository.findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(
            offender.getOffenderId(), DocumentType.PREVIOUS_CONVICTION
        );
        return OffenderTransformer.offenderSummaryOf(offender, document);
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetailSummary>> getMostLikelyOffenderSummaryByNomsNumber(String nomsNumber) {
        return offenderRepository.findMostLikelyByNomsNumber(nomsNumber).fold(Either::left,
            offender -> Either.right(offender.map(this::offenderSummaryWithPrecons)));
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetail>> getSingleOffenderByNomsNumber(String nomsNumber) {
        return tryToGetSingleOffenderByNomsNumber(nomsNumber).fold(Either::left,
            offender -> Either.right(offender.map(this::fullOffenderWithPrecons)));
    }

    public Either<DuplicateOffenderException, Optional<OffenderDetailSummary>> getSingleOffenderSummaryByNomsNumber(String nomsNumber) {
       return tryToGetSingleOffenderByNomsNumber(nomsNumber).fold(Either::left,
            offender -> Either.right(offender.map(this::offenderSummaryWithPrecons)));
    }

    public Either<DuplicateOffenderException, Optional<Offender>> tryToGetSingleOffenderByNomsNumber(String nomsNumber) {
        final var list = offenderRepository.findAllByNomsNumber(nomsNumber);
        switch (list.size()) {
            case 0:
                return Either.right(Optional.empty());
            case 1:
                return Either.right(Optional.of(list.get(0)));
            default:
                return Either.left(new DuplicateOffenderException(String.format("Expect a single offender with noms number %s but foud %d", nomsNumber, list
                    .size())));
        }
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

    @Transactional(readOnly = true)
    public List<PersonalContact> getOffenderPersonalContactsByCrn(String crn) {
        return offenderRepository.findByCrn(crn)
            .map(offender -> offender.getPersonalContacts().stream().map(PersonalContactTransformer::personalContactOf).collect(Collectors.toList()))
            .orElseThrow(() -> new NotFoundException("Offender not found"));
    }

    public StaffCaseloadEntry getManageSupervisionsEligibleOffenderByCrn(final String crn) {
        return offenderRepository.getOffenderWithOneActiveEventCommunitySentenceAndRarRequirementByCrn(crn)
            .map(OffenderTransformer::caseOf)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN '%s' is not eligible for the manage supervisions service", crn)));
    }
}
