package uk.gov.justice.digital.delius.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.data.api.ReferralSentResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsiManager;

import java.util.Collections;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class ReferralService {
    DeliusApiClient deliusApiClient;

    NsiService nsiService;

    OffenderService offenderService;

    public ReferralService(final DeliusApiClient deliusApiClient, final NsiService nsiService, final OffenderService offenderService) {
        this.deliusApiClient = deliusApiClient;
        this.nsiService = nsiService;
        this.offenderService = offenderService;
    }

    @Transactional
    public ReferralSentResponse createNsiReferral(final String crn,
                                                  final ReferralSentRequest referralSent) {
        var existingNsi = getExistingMatchingNsi(crn, referralSent);

        if(existingNsi.isPresent()) {
            return ReferralSentResponse.builder().nsiId(existingNsi.get().getNsiId()).build();
        }

        var newNsiRequest = NewNsi.builder()
            .type(referralSent.getNsiType())
            .subType(referralSent.getNsiSubType())
            .offenderCrn(crn)
            .eventId(referralSent.getConvictionId())
            .requirementId(referralSent.getRequirementId())
            .referralDate(referralSent.getDate())
            .status(referralSent.getNsiStatus())
            .statusDate(referralSent.getDate().atStartOfDay())
            .notes(referralSent.getNotes())
            .intendedProvider(referralSent.getProviderCode())
            .manager(NewNsiManager.builder()
                .staff(referralSent.getStaffCode())
                .team(referralSent.getTeamCode())
                .provider(referralSent.getProviderCode())
                .build()
            ).build();

        var newNsiReponse = deliusApiClient.createNewNsi(newNsiRequest);

        return ReferralSentResponse.builder().nsiId(newNsiReponse.getId()).build();
    }

    public Optional<Nsi> getExistingMatchingNsi(String crn, ReferralSentRequest referralSent) {
        // determine if there is an existing suitable NSI
        var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow();

        var existingNsis = nsiService.getNsiByCodes(offenderId, referralSent.getConvictionId(), Collections.singletonList(referralSent.getNsiType()))
            .map(wrapper -> wrapper.getNsis().stream()
                // eventID, offenderID, nsiID, and callerID are handled in the NSI service
                .filter(nsi -> Optional.ofNullable(nsi.getNsiSubType()).map(n -> n.getCode().equals(referralSent.getNsiSubType())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getReferralDate()).map(n -> n.equals(referralSent.getDate())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getNsiStatus()).map(n -> n.getCode().equals(referralSent.getNsiStatus())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getRequirement()).map(n -> nsi.getRequirement().getRequirementId().equals(referralSent.getRequirementId())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getIntendedProvider()).map(n -> n.getCode().equals(referralSent.getProviderCode())).orElse(false))
                .filter(nsi -> Optional.ofNullable(nsi.getNsiManagers()).map(n -> n.stream().anyMatch(nsiManager -> nsiManager.getStaff().getStaffCode().equals(referralSent.getStaffCode())
                    && nsiManager.getProbationArea().getCode().equals(referralSent.getProviderCode())
                    && nsiManager.getTeam().getCode().equals(referralSent.getTeamCode()))).orElse(false))
                .collect(toList())
            ).orElseThrow(() -> new RuntimeException("Offender ID not found"));

        if(existingNsis.size() > 1) {
            throw new RuntimeException("Multiple existing matching NSIs found");
        }
        return existingNsis.stream().findFirst();
    }
}
