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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
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
        var existingNsi = getExistingMatchingNsi(crn, contextName, referralStart.getSentenceId(),
            referralStart.getContractType(), referralStart.getStartedAt());

        var newNsiRequest = NewNsi.builder()
            .type(getNsiType(nsiMapping, referralStart.getContractType()))
            .offenderCrn(crn)
            .eventId(referralStart.getSentenceId())
            .requirementId(requirementId.orElse(null))
            .referralDate(toLondonLocalDate(referralStart.getStartedAt()))
            .startDate(toLondonLocalDate(referralStart.getStartedAt()))
            .status(nsiMapping.getNsiStatus())
            .statusDate(toLondonLocalDateTime(referralStart.getStartedAt()))
            .notes(referralStart.getNotes())
            .intendedProvider(context.getProviderCode())
            .manager(NewNsiManager.builder()
                .staff(context.getStaffCode())
                .team(context.getTeamCode())
                .provider(context.getProviderCode())
                .build()).build();

        Long newNsiId = deliusApiClient.createNewNsi(newNsiRequest).getId();
        return ReferralStartResponse.builder().nsiId(newNsiId).build();
    }

    @Transactional
    public ReferralEndResponse endNsiReferral(final String crn, final String contextName, final ContextlessReferralEndRequest request) {

        final var nsi = getExistingMatchingNsi(crn, contextName, request.getSentenceId(), request.getContractType(), request.getStartedAt())
            .orElseThrow(() -> new BadRequestException(format("Cannot find NSI for CRN: %s Sentence: %d and ContractType %s", crn, request.getSentenceId(), request.getContractType())));

        final var offenderId = offenderService.offenderIdOfCrn(crn)
            .orElseThrow(() -> new BadRequestException(format("Cannot find Offender Id for CRN: %s", crn)));
        deleteFutureAppointments(offenderId, contextName, nsi);

        final var jsonPatch = nsiPatchRequestTransformer.mapEndTypeToOutcomeOf(request, getContext(contextName));
        deliusApiClient.patchNsi(nsi.getNsiId(), jsonPatch);

        return new ReferralEndResponse(nsi.getNsiId());
    }

    public Optional<Nsi> getExistingMatchingNsi(final String crn,
                                                final String contextName,
                                                final Long sentenceId,
                                                final String contractType,
                                                final OffsetDateTime startedAt) {
        // determine if there is an existing suitable NSI
        var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow(() -> new BadRequestException("Offender CRN not found"));

        var context = getContext(contextName);
        var nsiMapping = context.getNsiMapping();

        var existingNsis = nsiService.getNsiByCodes(offenderId, sentenceId, Collections.singletonList(getNsiType(nsiMapping, contractType)))
            .map(wrapper -> wrapper.getNsis().stream()
                // eventID, offenderID, nsi type are handled in the NSI service
                .filter(nsi -> Optional.ofNullable(nsi.getReferralDate()).map(n -> n.equals(toLondonLocalDate(startedAt))).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getStatusDateTime()).map(n -> n.equals(toLondonLocalDateTime(startedAt.truncatedTo(ChronoUnit.SECONDS)))).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getNsiStatus()).map(n -> n.getCode().equals(nsiMapping.getNsiStatus())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getIntendedProvider()).map(n -> n.getCode().equals(context.getProviderCode())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getNsiManagers()).map(n -> n.stream().anyMatch(
                    nsiManager -> nsiManager.getStaff().getStaffCode().equals(context.getStaffCode())
                        && nsiManager.getTeam().getCode().equals(context.getTeamCode())
                        && nsiManager.getProbationArea().getCode().equals(context.getProviderCode())
                    )
                ).orElse(false))
                .collect(toList())
            ).orElse(Collections.emptyList());

        if (existingNsis.size() > 1) {
            throw new ConflictingRequestException("Multiple existing matching NSIs found");
        }
        return existingNsis.stream().findFirst();
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
        return Optional.ofNullable(nsiMapping.getContractTypeToNsiType().get(contactType)).orElseThrow(
            () -> new IllegalArgumentException("Nsi Type mapping from contractType does not exist for: " + contactType)
        );
    }

    IntegrationContext getContext(String contextName) {
        var context = deliusIntegrationContextConfig.getIntegrationContexts().get(contextName);
        return Optional.ofNullable(context).orElseThrow(
            () -> new IllegalArgumentException("IntegrationContext does not exist for: " + contextName)
        );
    }
}
