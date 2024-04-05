package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.SentenceStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAccessLimitations;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.UserAccessService;
import uk.gov.justice.digital.delius.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Tag(name = "Core offender", description = "Requires ROLE_COMMUNITY")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@Validated
public class OffendersResource {
    private final OffenderService offenderService;
    private final ConvictionService convictionService;
    private final NsiService nsiService;
    private final OffenderManagerService offenderManagerService;
    private final SentenceService sentenceService;
    private final UserService userService;
    private final UserAccessService userAccessService;

    @Operation(
        description = "Returns the current community and prison offender managers for an offender. Accepts an offender CRN in the format A999999",
        tags = {"Offender managers", "-- Popular core APIs --"})
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(path = "/offenders/crn/{crn}/allOffenderManagers")
    public List<CommunityOrPrisonOffenderManager> getAllOffenderManagersForOffenderbyCrn(
        @Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true)
        @NotNull
        @PathVariable(value = "crn") final String crn,
        @Parameter(name = "includeProbationAreaTeams", description = "include teams on the ProbationArea records", example = "true")
        @RequestParam(name = "includeProbationAreaTeams", required = false, defaultValue = "false") final boolean includeProbationAreaTeams) {
        return offenderManagerService.getAllOffenderManagersForCrn(crn, includeProbationAreaTeams)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN %s not found", crn)));
    }

    @Operation(
            description = "Returns the latest recall and release details for an offender. Accepts an offender CRN in the format A999999",
            tags = "Convictions")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/release")
    public OffenderLatestRecall getLatestRecallAndReleaseForOffenderByCrn(
            @Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true)
            @NotNull
            @PathVariable(value = "crn") final String crn) {

        return getOffenderLatestRecall(offenderService.offenderIdOfCrn(crn));
    }

    private OffenderLatestRecall getOffenderLatestRecall(final Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(offenderId -> offenderService.getOffenderLatestRecall(maybeOffenderId.get()))
                .orElseThrow(() -> new NotFoundException("Offender not found"));
    }

    @RequestMapping(value = "/offenders/crn/{crn}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
            @ApiResponse(responseCode = "404", description = "The offender not found")
    })
    @Operation(description = "Returns the offender summary for the given crn", tags = "-- Popular core APIs --")

    public OffenderDetailSummary getOffenderSummaryByCrn(final @PathVariable("crn") String crn, Authentication authentication) {
        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());

        return offenderService.getOffenderSummaryByCrn(crn)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/all", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "The offender is not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
    })
    @Operation(description = "Returns the full offender detail for the given crn", tags = "-- Popular core APIs --")
    public OffenderDetail getOffenderDetailByCrn(final @PathVariable("crn") String crn, Authentication authentication) {
        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());

        final var offender = offenderService.getOffenderByCrn(crn);
        return offender.orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/all", method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "The offender is not found"),
        @ApiResponse(responseCode = "409", description = "Multiple offenders found in the same state")
    })
    @Operation(description = "Returns the full offender detail for the given nomsNumber. If multiple offender found the active one will be returned", tags = "-- Popular core APIs --")
    public OffenderDetail getOffenderDetailByNomsNumber(
        @Parameter(name = "nomsNumber", description = "Nomis number for the offender", example = "G9542VP", required = true)
        @NotNull @PathVariable(value = "nomsNumber")
        final String nomsNumber,
        @Parameter(name = "failOnDuplicate", description = "Should fail if multiple offenders found", example = "true")
        final @RequestParam(value = "failOnDuplicate", defaultValue = "false") boolean failOnDuplicate
    ) {
        final Optional<OffenderDetail> offender;
        if (failOnDuplicate) {
            offender = offenderService
                .getSingleOffenderByNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));

        } else {
            offender = offenderService
                .getMostLikelyOffenderByNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));
        }
        return offender.orElseThrow(() -> new NotFoundException(String.format("Offender with nomsNumber %s not found", nomsNumber)));
    }

    @Operation(description = "Return the convictions (AKA Delius Event) for an offender", tags = {"-- Popular core APIs --", "Convictions"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "The offender is not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions")
    public List<Conviction> getConvictionsForOffenderByCrn(
            @Parameter(name = "crn", description = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @Parameter(name = "activeOnly", description = "retrieve only active convictions", example = "true")
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly) {

        return offenderService.offenderIdOfCrn(crn)
                .map(offenderId -> convictionService.convictionsFor(offenderId, activeOnly))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @Operation(description = "Return the conviction (AKA Delius Event) for a conviction ID and a CRN", tags = {"-- Popular core APIs --", "Convictions"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "The offender CRN or conviction ID is not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}")
    public Conviction getConvictionForOffenderByCrnAndConvictionId(
            @Parameter(description = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @Parameter(description = "ID for the conviction / event", example = "2500295345", required = true)
            @NotNull @PathVariable(value = "convictionId") final Long convictionId) {

        return offenderService.offenderIdOfCrn(crn)
                .map((offenderId) -> convictionService.convictionFor(offenderId, convictionId))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)))
                .orElseThrow(() -> new NotFoundException(String.format("Conviction with ID %s for Offender with crn %s not found", convictionId, crn)));
    }

    @Operation(description = "Return the NSIs for a conviction ID and a CRN, filtering by NSI codes", tags = "Sentence requirements and breach")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "The offender CRN is not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/nsis")
    public NsiWrapper getNsiForOffenderByCrnAndConvictionId(
            @Parameter(name = "crn", description = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @Parameter(name = "convictionId", description = "ID for the conviction / event", example = "2500295345", required = true)
            @NotNull @PathVariable(value = "convictionId") final Long convictionId,
            @Parameter(name = "nsiCodes", description = "list of NSI codes to constrain by", example = "BRE,BRES", required = true)
            @NotEmpty @RequestParam(value = "nsiCodes") final List<String> nsiCodes) {

        return offenderService.offenderIdOfCrn(crn)
                .map((offenderId) -> nsiService.getNsiByCodes(offenderId, convictionId, nsiCodes))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)))
                .orElseThrow(() -> new NotFoundException(String.format("Conviction with ID %s for Offender with crn %s not found", convictionId, crn)));
    }

    @Operation(description = "Return all the NSIs for the CRN, active convictions only, filtering by NSI codes", tags = "Sentence requirements and breach")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "The offender CRN is not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/active/nsis")
    public NsiWrapper getNsisForOffenderByCrnAndActiveConvictions(
            @Parameter(name = "crn", description = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @Parameter(name = "nsiCodes", description = "list of NSI codes to constrain by", example = "BRE,BRES", required = true)
            @NotEmpty @RequestParam(value = "nsiCodes") final List<String> nsiCodes) {

        return offenderService.offenderIdOfCrn(crn)
                .map((offenderId) -> nsiService.getNsiByCodesForOffenderActiveConvictions(offenderId, nsiCodes))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @Operation(description = "Return an NSI by crn, convictionId and nsiId", tags = "Convictions")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "The offender CRN is not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/nsis/{nsiId}")
    public Nsi getNsiByNsiId(
            @Parameter(name = "crn", description = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @Parameter(name = "convictionId", description = "ID for the conviction / event", example = "2500295345", required = true)
            @NotNull @PathVariable(value = "convictionId") final Long convictionId,
            @Parameter(name = "nsiId", description = "ID for the nsi", example = "2500295123", required = true)
            @PathVariable(value = "nsiId") final Long nsiId) {
        return offenderService.getOffenderByCrn(crn)
                .map((offender) -> convictionService.convictionFor(offender.getOffenderId(), convictionId)
                    .orElseThrow(() -> new NotFoundException(String.format("Conviction with convictionId %s not found for offender with crn %s", convictionId, crn)))
                ).map(conviction -> nsiService.getNsiById(nsiId))
                .orElseThrow(() -> new NotFoundException(String.format("NSI with id %s not found", nsiId)))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @Operation(description = "Return sentence and custodial status information by crn, convictionId.")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "The offender CRN / conviction ID is not found"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/sentenceStatus")
    public Optional<SentenceStatus> getSentenceStatusByConvictionId(
        @Parameter(name = "crn", description = "CRN for the offender", example = "A123456", required = true)
        @PathVariable(value = "crn") final String crn,
        @Parameter(name = "convictionId", description = "ID for the conviction / event", example = "2500295345", required = true)
        @PathVariable(value = "convictionId") final Long convictionId) {

        var sentence = offenderService.offenderIdOfCrn(crn)
            .map((offenderId) -> convictionService.convictionFor(offenderId, convictionId))
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)))
            .orElseThrow(() -> new NotFoundException(String.format("Conviction with ID %s for Offender with crn %s not found", convictionId, crn)))
            .getSentence();

        return Optional.ofNullable(sentence)
            .map(s -> sentenceService.getSentenceStatus(crn, convictionId, s.getSentenceId()))
            .orElseThrow(() -> new NotFoundException(String.format("Sentence not found for crn '%s', convictionId '%s'", crn, convictionId)));
    }

    @Hidden // This endpoint is too specific to use case and does not reflect best practice so is deprecated for new use
    @Deprecated
    @Operation(description = "Return sentence and custodial status information by crn, convictionId and sentenceId.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "The offender CRN / conviction ID / sentence ID is not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/sentences/{sentenceId}/status")
    public SentenceStatus getSentenceStatusBySentenceId(
            @Parameter(name = "crn", description = "CRN for the offender", example = "A123456", required = true)
            @PathVariable(value = "crn") final String crn,
            @Parameter(name = "convictionId", description = "ID for the conviction / event", example = "2500295345", required = true)
            @PathVariable(value = "convictionId") final Long convictionId,
            @Parameter(name = "sentenceId", description = "ID for the sentence", example = "2500295123", required = true)
            @PathVariable(value = "sentenceId") final Long sentenceId) {

        return sentenceService.getSentenceStatus(crn, convictionId, sentenceId)
                .orElseThrow(() -> new NotFoundException(String.format("Sentence not found for crn '%s', convictionId '%s', and sentenceId '%s'", crn, convictionId, sentenceId)));
    }

    @Operation(description = "Reveals if the specified user can access details about the supplied offender", tags = "Authentication and users")
    @RequestMapping(value = "/offenders/crn/{crn}/user/{username}/userAccess", method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "403", description = "User is restricted from access to offender"),
        @ApiResponse(responseCode = "404", description = "No such offender, or no such User (see body for detail)")
    })
    public ResponseEntity<AccessLimitation> checkUserAccessByCrn(
        final @PathVariable("crn") String crn,
        final @PathVariable("username") String username) {
        return offenderService.getOffenderAccessLimitationsByCrn(crn)
            .map(offender -> accessLimitationResponseEntityOf(offender, username))
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    private ResponseEntity<AccessLimitation> accessLimitationResponseEntityOf(final OffenderAccessLimitations offender, final String username) {
        final var accessLimitation = userService.accessLimitationOf(username, offender);
        return new ResponseEntity<>(accessLimitation, (accessLimitation.isUserExcluded() || accessLimitation.isUserRestricted()) ? FORBIDDEN : OK);
    }
}
