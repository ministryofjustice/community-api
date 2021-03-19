package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.data.api.ReferralSentResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsiManager;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class ReferralService {

    private final String providerCode;
    private final String staffCode;
    private final String teamCode;
    private final String nsiStatus;
    private final Map<String, String> referralTypeToNsiTypeMapping;

    private final DeliusApiClient deliusApiClient;

    private final NsiService nsiService;

    private final OffenderService offenderService;

    public ReferralService(final DeliusApiClient deliusApiClient,
                           final NsiService nsiService,
                           final OffenderService offenderService,
                           @Value("${new-nsi.referral.provider-code}") final String providerCode,
                           @Value("${new-nsi.referral.staff-code}") final String staffCode,
                           @Value("${new-nsi.referral.team-code}") final String teamCode,
                           @Value("${new-nsi.referral.nsi-status}") final String nsiStatus,
                           @Value("#{${new-nsi.referral.referral-type-to-nsi-type}}") final Map<String, String> referralTypeToNsiTypeMapping
    ) {
        this.deliusApiClient = deliusApiClient;
        this.nsiService = nsiService;
        this.offenderService = offenderService;
        this.providerCode = providerCode;
        this.staffCode = staffCode;
        this.teamCode = teamCode;
        this.nsiStatus = nsiStatus;
        this.referralTypeToNsiTypeMapping = referralTypeToNsiTypeMapping;
    }

    @Transactional
    public ReferralSentResponse createNsiReferral(final String crn,
                                                  final ReferralSentRequest referralSent) {
        var existingNsi = getExistingMatchingNsi(crn, referralSent);

        return ReferralSentResponse.builder().nsiId(existingNsi.map(Nsi::getNsiId).orElseGet(() -> {
            var newNsiRequest = NewNsi.builder()
                .type(getNsiType(referralSent.getServiceCategory()))
                .offenderCrn(crn)
                .eventId(referralSent.getConvictionId())
                .requirementId(referralSent.getRequirementId())
                .referralDate(referralSent.getDate())
                .status(nsiStatus)
                .statusDate(referralSent.getDate().atStartOfDay())
                .notes(referralSent.getNotes())
                .intendedProvider(providerCode)
                .manager(NewNsiManager.builder()
                    .staff(staffCode)
                    .team(teamCode)
                    .provider(providerCode)
                    .build()).build();

            return deliusApiClient.createNewNsi(newNsiRequest).getId();
        })).build();
    }

    public Optional<Nsi> getExistingMatchingNsi(String crn, ReferralSentRequest referralSent) {
        // determine if there is an existing suitable NSI
        var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow(() -> new BadRequestException("Offender CRN not found"));

        var existingNsis = nsiService.getNsiByCodes(offenderId, referralSent.getConvictionId(), Collections.singletonList(getNsiType(referralSent.getServiceCategory())))
            .map(wrapper -> wrapper.getNsis().stream()
                // eventID, offenderID, nsiID, and callerID are handled in the NSI service
                .filter(nsi -> Optional.ofNullable(nsi.getReferralDate()).map(n -> n.equals(referralSent.getDate())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getNsiStatus()).map(n -> n.getCode().equals(nsiStatus)).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getRequirement()).map(n -> nsi.getRequirement().getRequirementId().equals(referralSent.getRequirementId())).orElse(referralSent.getRequirementId() == null))
                .filter(nsi -> Optional.ofNullable(nsi.getIntendedProvider()).map(n -> n.getCode().equals(providerCode)).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getNsiManagers()).map(n -> n.stream().anyMatch(
                    nsiManager -> nsiManager.getStaff().getStaffCode().equals(staffCode)
                        && nsiManager.getTeam().getCode().equals(teamCode)
                        && nsiManager.getProbationArea().getCode().equals(providerCode)
                    )
                ).orElse(false))
                .collect(toList())
            ).orElse(Collections.emptyList());

        if (existingNsis.size() > 1) {
            throw new ConflictingRequestException("Multiple existing matching NSIs found");
        }
        return existingNsis.stream().findFirst();
    }

    String getNsiType(final String referralType) {
        return Optional.ofNullable(referralTypeToNsiTypeMapping.get(referralType)).orElseThrow(
            () -> new IllegalArgumentException("Nsi Type mapping from referralType does not exist for: " + referralType)
        );
    }
}
