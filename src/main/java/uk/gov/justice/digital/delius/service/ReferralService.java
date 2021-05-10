package uk.gov.justice.digital.delius.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.NsiMapping;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.data.api.ReferralSentResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsiManager;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDateTime;

@Service
public class ReferralService {

    private final DeliusApiClient deliusApiClient;

    private final NsiService nsiService;

    private final OffenderService offenderService;

    private final RequirementService requirementService;

    private final DeliusIntegrationContextConfig deliusIntegrationContextConfig;

    public ReferralService(final DeliusApiClient deliusApiClient,
                           final NsiService nsiService,
                           final OffenderService offenderService,
                           final RequirementService requirementService,
                           final DeliusIntegrationContextConfig deliusIntegrationContextConfig
                           ) {
        this.deliusApiClient = deliusApiClient;
        this.nsiService = nsiService;
        this.offenderService = offenderService;
        this.requirementService = requirementService;
        this.deliusIntegrationContextConfig = deliusIntegrationContextConfig;
    }

    @Transactional
    public ReferralSentResponse createNsiReferral(final String crn,
                                                  final ReferralSentRequest referralSent) {

        var context = getContext(referralSent.getContext());
        var nsiMapping = context.getNsiMapping();

        Long requirementId = getRequirement(crn, referralSent.getSentenceId(), context);
        var existingNsi = getExistingMatchingNsi(crn, referralSent, requirementId);

        return ReferralSentResponse.builder().nsiId(existingNsi.map(Nsi::getNsiId).orElseGet(() -> {
            var newNsiRequest = NewNsi.builder()
                .type(getNsiType(nsiMapping, referralSent.getServiceCategoryId()))
                .offenderCrn(crn)
                .eventId(referralSent.getSentenceId())
                .requirementId(requirementId)
                .referralDate(toLondonLocalDate(referralSent.getSentAt()))
                .status(nsiMapping.getNsiStatus())
                .statusDate(toLondonLocalDateTime(referralSent.getSentAt()))
                .notes(referralSent.getNotes())
                .intendedProvider(context.getProviderCode())
                .manager(NewNsiManager.builder()
                    .staff(context.getStaffCode())
                    .team(context.getTeamCode())
                    .provider(context.getProviderCode())
                    .build()).build();

            return deliusApiClient.createNewNsi(newNsiRequest).getId();
        })).build();
    }

    public Optional<Nsi> getExistingMatchingNsi(String crn, ReferralSentRequest referralSent, Long requirementId) {
        // determine if there is an existing suitable NSI
        var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow(() -> new BadRequestException("Offender CRN not found"));

        var context = getContext(referralSent.getContext());
        var nsiMapping = context.getNsiMapping();

        var existingNsis = nsiService.getNsiByCodes(offenderId, referralSent.getSentenceId(), Collections.singletonList(getNsiType(nsiMapping, referralSent.getServiceCategoryId())))
            .map(wrapper -> wrapper.getNsis().stream()
                // eventID, offenderID, nsiID, and callerID are handled in the NSI service
                .filter(nsi -> Optional.ofNullable(nsi.getReferralDate()).map(n -> n.equals(toLondonLocalDate(referralSent.getSentAt()))).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getNsiStatus()).map(n -> n.getCode().equals(nsiMapping.getNsiStatus())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getRequirement()).map(n -> nsi.getRequirement().getRequirementId().equals(requirementId)).orElse(false))
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

    Long getRequirement(String crn, Long sentenceId, IntegrationContext context) {
        return requirementService.getRequirement(crn, sentenceId, context.getRequirementRehabilitationActivityType()).getRequirementId();
    }

    String getNsiType(final NsiMapping nsiMapping, final UUID serviceCategoryId) {
        return Optional.ofNullable(nsiMapping.getServiceCategoryToNsiType().get(serviceCategoryId)).orElseThrow(
            () -> new IllegalArgumentException("Nsi Type mapping from referralType does not exist for: " + serviceCategoryId)
        );
    }

    IntegrationContext getContext(String contextName) {
        var context = deliusIntegrationContextConfig.getIntegrationContexts().get(contextName);
        return Optional.ofNullable(context).orElseThrow(
            () -> new IllegalArgumentException("IntegrationContext does not exist for: " + contextName)
        );
    }

}
