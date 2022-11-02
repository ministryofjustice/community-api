package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.NsiMapping;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralEndRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralStartRequest;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.ReferralEndResponse;
import uk.gov.justice.digital.delius.data.api.ReferralStartResponse;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsiManager;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.transformers.NsiPatchRequestTransformer;
import uk.gov.justice.digital.delius.transformers.ReferralTransformer;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.validation.constraints.NotNull;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDateTime;

@Service
@AllArgsConstructor
public class ReferralService {
    private final TelemetryClient telemetryClient;

    private final DeliusApiClient deliusApiClient;

    private final NsiService nsiService;

    private final OffenderService offenderService;

    private final RequirementService requirementService;

    private final NsiPatchRequestTransformer nsiPatchRequestTransformer;

    private final ContactRepository contactRepository;

    private final DeliusIntegrationContextConfig deliusIntegrationContextConfig;

    @Transactional
    public ReferralStartResponse startNsiReferral(final String crn,
                                                  final String contextName,
                                                  final ContextlessReferralStartRequest referralStart) {

        var context = getContext(contextName);
        var nsiMapping = context.getNsiMapping();
        var urn = ReferralTransformer.transformReferralIdToUrn(referralStart.getReferralId());

        Optional<Long> requirementId = getRequirement(crn, referralStart.getSentenceId(), context);

        final var newNsiRequest = NewNsi.builder()
            .type(getNsiType(nsiMapping, referralStart.getContractType()))
            .offenderCrn(crn)
            .eventId(referralStart.getSentenceId())
            .requirementId(requirementId.orElse(null))
            .referralDate(toLondonLocalDate(referralStart.getStartedAt()))
            .startDate(toLondonLocalDate(referralStart.getStartedAt()))
            .status(nsiMapping.getNsiStatus())
            .statusDate(toLondonLocalDateTime(referralStart.getStartedAt()))
            .externalReference(urn)
            .notes(ReferralTransformer.prefixReferralStartNotesWithUrn(referralStart.getNotes(), referralStart.getReferralId()))
            .intendedProvider(context.getProviderCode())
            .manager(NewNsiManager.builder()
                .staff(context.getStaffCode())
                .team(context.getTeamCode())
                .provider(context.getProviderCode())
                .build()).build();

        final var newNsi = deliusApiClient.createNewNsi(newNsiRequest);
        return ReferralStartResponse.builder().nsiId(newNsi.getId()).build();
    }

    @Transactional
    public ReferralEndResponse endNsiReferral(final String crn, final String contextName, final ContextlessReferralEndRequest request) {

        final var nsi = getExistingMatchingNsi(crn, contextName, request.getSentenceId(), request.getContractType(), request.getStartedAt(), request.getReferralId())
            .orElseThrow(() -> new BadRequestException(format("Cannot find NSI for CRN: %s Sentence: %d and ContractType %s", crn, request.getSentenceId(), request.getContractType())));

        final var offenderId = offenderService.offenderIdOfCrn(crn)
            .orElseThrow(() -> new BadRequestException(format("Cannot find Offender Id for CRN: %s", crn)));
        deleteFutureAppointments(offenderId, contextName, nsi);

        final var jsonPatch = nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(request, getContext(contextName));
        deliusApiClient.patchNsi(nsi.getNsiId(), jsonPatch);

        return new ReferralEndResponse(nsi.getNsiId());
    }

    public Optional<Nsi> getExistingMatchingNsi(@NotNull final String crn,
                                                @NotNull final String contextName,
                                                @NotNull final Long sentenceId,
                                                @NotNull final String contractType,
                                                @NotNull final OffsetDateTime startedAt,
                                                final UUID referralId) {
        // determine if there is an existing suitable NSI
        var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow(() -> new BadRequestException("Offender CRN not found"));

        return findReferralNSIByURN(referralId, offenderId)
            .or(() -> findReferralNSIByFuzzyMatching(crn, contextName, sentenceId, contractType, startedAt, referralId, offenderId));
    }

    private Optional<Nsi> findReferralNSIByURN(UUID referralId, Long offenderId) {
        var urn = ReferralTransformer.transformReferralIdToUrn(referralId);
        var existingNsis = nsiService.getNsisInAnyStateByExternalReferenceURN(offenderId, urn);
        return ensureZeroOrOneNsi(existingNsis, referralId, "Multiple existing URN NSIs found");
    }

    private Optional<Nsi> findReferralNSIByFuzzyMatching(String crn, String contextName, Long sentenceId, String contractType, OffsetDateTime startedAt, UUID referralId, Long offenderId) {
        // 2022-10-26: external_reference URNs rollout should mean this branch will not be needed in about ~90 days
        // measure by the below event having 0 occurrences
        var referralIdString = Optional.ofNullable(referralId).map(UUID::toString).orElse("missing");
        telemetryClient.trackEvent(
            "community_api.get_existing_matching_nsi.fuzzy_match_fallback",
            Map.of("crn", crn, "contextName", contextName, "startedAt", startedAt.toString(), "referralId", referralIdString),
            null
        );

        var context = getContext(contextName);
        var nsiMapping = context.getNsiMapping();
        var existingNsis = nsiService.getNsiByCodes(offenderId, sentenceId, Collections.singletonList(getNsiType(nsiMapping, contractType)))
            .map(wrapper -> wrapper.getNsis().stream()
                // eventID, offenderID, nsi type are handled in the NSI service
                .filter(nsi -> !nsi.getSoftDeleted())
                .filter(nsi -> ofNullable(nsi.getReferralDate()).map(n -> n.equals(toLondonLocalDate(startedAt))).orElse(false))
                .filter(nsi -> ofNullable(nsi.getNsiStatus()).map(n -> n.getCode().equals(nsiMapping.getNsiStatus())).orElse(false))
                .filter(nsi -> Objects.isNull(nsi.getNsiOutcome()))
                .filter(nsi -> ofNullable(nsi.getIntendedProvider()).map(n -> n.getCode().equals(context.getProviderCode())).orElse(false))
                .filter(nsi -> ofNullable(nsi.getNsiManagers()).map(n -> n.stream().anyMatch(
                    nsiManager -> nsiManager.getStaff().getStaffCode().equals(context.getStaffCode())
                        && nsiManager.getTeam().getCode().equals(context.getTeamCode())
                        && nsiManager.getProbationArea().getCode().equals(context.getProviderCode())
                    )
                ).orElse(false))
                .collect(toList())
            ).orElse(Collections.emptyList());

        return ensureZeroOrOneNsi(existingNsis, referralId, "Multiple existing matching NSIs found");
    }

    private Optional<Nsi> ensureZeroOrOneNsi(List<Nsi> nsis, UUID referralId, String errorMessageForMultiples) {
        if (nsis.size() > 1) {
            throw new BadRequestException(format("%s for referral: %s, NSI IDs: %s", errorMessageForMultiples, referralId, nsis.stream().map(Nsi::getNsiId).toList()));
        }
        if (nsis.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(nsis.get(0));
    }

    void deleteFutureAppointments(Long offenderId, String contextName, Nsi nsi) {

        final var context = getContext(contextName);
        final var applicableContactTypes = context.getContactMapping().getAllAppointmentContactTypes();
        final var today = LocalDate.now();

        contactRepository.findByOffenderAndNsiId(offenderId, nsi.getNsiId()).stream()
            .filter(contact -> Objects.isNull(contact.getContactOutcomeType())) // Have no existing outcome
            .filter(contact -> applicableContactTypes.contains(contact.getContactType().getCode())) // Of the correct contact type
            .filter(contact -> !contact.getContactDate().isBefore(today)) // Is not historic
            .forEach(contact -> deliusApiClient.deleteContact(contact.getContactId()));
    }

    Optional<Long> getRequirement(String crn, Long sentenceId, IntegrationContext context) {
        return requirementService.getActiveRequirement(crn, sentenceId, context.getRequirementRehabilitationActivityType())
            .map(Requirement::getRequirementId);
    }

    String getNsiType(final NsiMapping nsiMapping, final String contactType) {
        return ofNullable(nsiMapping.getContractTypeToNsiType().get(contactType)).orElseThrow(
            () -> new BadRequestException("Nsi Type mapping from contractType does not exist for: " + contactType)
        );
    }

    IntegrationContext getContext(String contextName) {
        var context = deliusIntegrationContextConfig.getIntegrationContexts().get(contextName);
        return ofNullable(context).orElseThrow(
            () -> new BadRequestException("IntegrationContext does not exist for: " + contextName)
        );
    }
}
