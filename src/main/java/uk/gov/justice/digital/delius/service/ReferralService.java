package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsiManager;
import uk.gov.justice.digital.delius.data.api.deliusapi.NsiDto;

import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class ReferralService {
    private final WebClient webClient;

    private final OffenderService offenderService;

    private final NsiService nsiService;

    @Autowired
    public ReferralService(@Qualifier("deliusApiWebClient") final WebClient webClient, final OffenderService offenderService, final NsiService nsiService) {
        this.webClient = webClient;
        this.offenderService = offenderService;
        this.nsiService = nsiService;
    }

    @Transactional
    public ResponseEntity<String> createReferralSent(final String crn,
                                                     final ReferralSentRequest referralSent) {

        var nsiId = createOrGetNsiId(crn, referralSent);

        var contact = NewContact.builder()
            .offenderCrn(crn)
            .type(referralSent.getReferralType())
            .provider(referralSent.getProviderCode())
            .team(referralSent.getTeamCode())
            .staff(referralSent.getStaffCode())
            .date(referralSent.getDate())
            .notes(referralSent.getNotes())
            .eventId(referralSent.getConvictionId())
            .requirementId(referralSent.getRequirementId())
            //.nsiId(nsiId) //TODO: currently not possible to associate with NSI
            .build();

        return webClient.post()
            .uri("/v1/contact")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(contact)
            .retrieve()
            .toEntity(String.class)
            .block();
    }

    private Optional<Nsi> getExistingMatchingNsi(String crn, ReferralSentRequest referralSent) {
        // determine if there is an existing suitable NSI
        var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow();

        var existingNsis = nsiService.getNsiByCodes(offenderId, referralSent.getConvictionId(), Arrays.asList(referralSent.getNsiType()))
            .map(wrapper -> wrapper.getNsis().stream()
                .filter(nsi -> nsi.getNsiSubType().getCode().equals(referralSent.getNsiSubType()))
                .filter(nsi -> nsi.getReferralDate().equals(referralSent.getDate()))
                .filter(nsi -> nsi.getNsiStatus().equals(referralSent.getNsiStatus()))
                .filter(nsi -> nsi.getRequirement().getRequirementId().equals(referralSent.getRequirementId()))
                .filter(nsi -> nsi.getIntendedProvider().getCode().equals(referralSent.getIntendedProvider()))
                .filter(nsi -> nsi.getNsiManagers().stream().anyMatch(nsiManager -> nsiManager.getStaff().getStaffCode().equals(referralSent.getStaffCode())
                    && nsiManager.getProbationArea().getCode().equals(referralSent.getProviderCode())
                    && nsiManager.getTeam().getCode().equals(referralSent.getTeamCode())))
                .collect(toList())
            ).orElseThrow(() -> new RuntimeException("Offender ID not found"));

        if(existingNsis.size() > 1) {
            throw new RuntimeException("Multiple existing matching NSIs found");
        }
        return Optional.of(existingNsis.get(0));
    }

    private Long createOrGetNsiId(String crn, ReferralSentRequest referralSent) {
        var existingNsi = getExistingMatchingNsi(crn, referralSent);

        if(existingNsi.isPresent()) {
            return existingNsi.get().getNsiId();
        }

        var newNsi = NewNsi.builder()
            .type(referralSent.getNsiType())
            .subType(referralSent.getNsiSubType())
            .offenderCrn(crn)
            .eventId(referralSent.getConvictionId())
            .requirementId(referralSent.getRequirementId())
            .referralDate(referralSent.getDate())
            .status(referralSent.getNsiStatus())
            .statusDate(referralSent.getDate().atStartOfDay())
            .notes(referralSent.getNsiNotes())
            .intendedProvider(referralSent.getIntendedProvider())
            .manager(NewNsiManager.builder()
                .staff(referralSent.getStaffCode())
                .team(referralSent.getTeamCode())
                .provider(referralSent.getProviderCode())
                .build()
            ).build();

        return webClient.post()
            .uri("/v1/nsi")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(newNsi)
            .retrieve()
            .toEntity(NsiDto.class)
            .block().getBody().getId();
    }
}
