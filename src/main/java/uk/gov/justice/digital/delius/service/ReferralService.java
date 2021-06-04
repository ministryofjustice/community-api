package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.NsiMapping;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.validation.constraints.NotNull;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.ReferralTransformer.transformReferralIdToUrn;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDateTime;

@Service
@AllArgsConstructor
public class ReferralService {

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

        var context = getContext(contextName);
        var nsiMapping = context.getNsiMapping();

        var existingNsis = nsiService.getNsiByCodes(offenderId, sentenceId, Collections.singletonList(getNsiType(nsiMapping, contractType)))
            .map(wrapper -> wrapper.getNsis().stream()
                // eventID, offenderID, nsi type are handled in the NSI service
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

        if (existingNsis.size() > 1) {
            existingNsis = findExistingNsiByReferralUrnIfSupplied(existingNsis, referralId);
        }
        return existingNsis.stream().findFirst();
    }

    /**
     * There is an edge case when more that one Referral may be created in R&M that appear to cover the same Intervention,
     * i.e. are for the same CRN and Sentence, cover the same Contract Type (e.g. Personal Wellbeing) and start on the same date.
     * It is suspected that this is a rare scenario but can arise.
     * This method comes into play when that situation arises and uses the referral id (in the form of a URN) to carry out a
     * further level of filtering. This is only as a last resort when the normal filtering criteria doesn't identify a unique
     * match and when the request provides a referral id.
     * If after this level of filtering there are still duplicates an exception is thrown.
     */
    @NotNull
    List<Nsi> findExistingNsiByReferralUrnIfSupplied(@NotNull final List<Nsi> existingNsis, final UUID referralId) {
        final var filteredNsis = ofNullable(referralId)
            .map(id -> filterExistingNsisByReferralUrn(existingNsis, id))
            .orElse(existingNsis);

        if (filteredNsis.size() > 1) {
            throw new ConflictingRequestException("Multiple existing matching NSIs found");
        }
        return filteredNsis;
    }

    /**
     * The URN is held as the first entry in the notes field. This may seem unsafe, however there is no other place to store it
     * in Delius, and it is guaranteed that the notes field in Delius is only ever appended to and existing state never changed.
     */
    @NotNull
    List<Nsi> filterExistingNsisByReferralUrn(@NotNull final List<Nsi> existingNsis, @NotNull final UUID referralId) {
        final var referralUrn = transformReferralIdToUrn(referralId).toLowerCase();
        return existingNsis.stream()
            .filter(nsi -> nsi.getNotes().toLowerCase().startsWith(referralUrn))
            .toList();
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
        return requirementService.getRequirement(crn, sentenceId, context.getRequirementRehabilitationActivityType())
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
