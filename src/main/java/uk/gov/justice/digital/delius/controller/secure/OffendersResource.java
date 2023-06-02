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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.ContactSummary;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;
import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.PersonalContact;
import uk.gov.justice.digital.delius.data.api.ProbationStatusDetail;
import uk.gov.justice.digital.delius.data.api.SentenceStatus;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAccessLimitations;
import uk.gov.justice.digital.delius.service.AssessmentService;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.UserAccessService;
import uk.gov.justice.digital.delius.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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

    private static final String INITIAL_APPOINTMENT_CONTACT_TYPE = "COAI";
    private static final String INITIAL_APPOINTMENT_VIDEO_CONTACT_TYPE = "COVI";
    private static final String INITIAL_APPOINTMENT_DOORSTEP_CONTACT_TYPE = "CODI";
    private static final String INITIAL_APPOINTMENT_HOME_VISIT_CONTACT_TYPE = "COHV";
    private static final List<String> INITIAL_APPOINTMENT_CONTACT_TYPES = Arrays.asList(INITIAL_APPOINTMENT_CONTACT_TYPE, INITIAL_APPOINTMENT_VIDEO_CONTACT_TYPE, INITIAL_APPOINTMENT_DOORSTEP_CONTACT_TYPE,INITIAL_APPOINTMENT_HOME_VISIT_CONTACT_TYPE);
    private final OffenderService offenderService;
    private final ContactService contactService;
    private final ConvictionService convictionService;
    private final NsiService nsiService;
    private final OffenderManagerService offenderManagerService;
    private final SentenceService sentenceService;
    private final UserService userService;
    private final CurrentUserSupplier currentUserSupplier;
    private final CustodyService custodyService;
    private final UserAccessService userAccessService;
    private final AssessmentService assessmentService;

    @Operation(
            description = "Returns the current community and prison offender managers for an offender. Accepts a NOMIS offender nomsNumber in the format A9999AA",
            tags = {"Offender managers", "-- Popular core APIs --"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/allOffenderManagers")
    public List<CommunityOrPrisonOffenderManager> getAllOffenderManagersForOffender(
        @Parameter(name = "nomsNumber", description = "Nomis number for the offender", example = "G9542VP", required = true)
        @NotNull
        @PathVariable(value = "nomsNumber") final String nomsNumber,
        @Parameter(name = "includeProbationAreaTeams", description = "include teams on the ProbationArea records", example = "true")
        @RequestParam(name = "includeProbationAreaTeams", required = false, defaultValue = "false") final boolean includeProbationAreaTeams) {
        return offenderManagerService.getAllOffenderManagersForNomsNumber(nomsNumber, includeProbationAreaTeams)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with NOMS number %s not found", nomsNumber)));
    }

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

    @Operation(description = "Return the convictions (AKA Delius Event) for an offender", tags = {"Convictions","-- Popular core APIs --"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "409", description = "Multiple offenders found in the same state ")
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/convictions")
    public List<Conviction> getConvictionsForOffender(
            @Parameter(name = "nomsNumber", description = "Nomis number for the offender", example = "G9542VP", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
            @Parameter(name = "activeOnly", description = "retrieve only active convictions", example = "true")
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly,
            @Parameter(name = "failOnDuplicate", description = "Should fail if multiple offenders found regardless of status", example = "true")
            final @RequestParam(value = "failOnDuplicate", defaultValue = "false") boolean failOnDuplicate) {

        final Optional<Long> mayBeOffenderId;
        if (failOnDuplicate) {
            mayBeOffenderId = offenderService
                .singleOffenderIdOfNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));

        } else {
            mayBeOffenderId = offenderService
                .mostLikelyOffenderIdOfNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));
        }

        return mayBeOffenderId
                .map(offenderId -> convictionService.convictionsFor(offenderId, activeOnly))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with nomsNumber %s not found", nomsNumber)));
    }

    @Operation(description = "Return the details for an offender. If multiple offenders found the active one wll be returned", tags = "-- Popular core APIs --")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Multiple offenders found in the same state ")
        })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}")
    public OffenderDetailSummary getOffenderDetails(
        @Parameter(name = "nomsNumber", description = "Nomis number for the offender", example = "G9542VP", required = true)
        @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
        @Parameter(name = "failOnDuplicate", description = "Should fail if multiple offenders found regardless of status", example = "true")
        final @RequestParam(value = "failOnDuplicate", defaultValue = "false") boolean failOnDuplicate
    ) {
        final Optional<OffenderDetailSummary> offender;
        if (failOnDuplicate) {
            offender = offenderService
                .getSingleOffenderSummaryByNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));

        } else {
            offender = offenderService
                .getMostLikelyOffenderSummaryByNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));
        }
        return offender.orElseThrow(() -> new NotFoundException(String.format("Offender with nomsNumber %s not found", nomsNumber)));
    }

    @Operation(description = "Returns the induction appointments for an offender by CRN", tags = "Contact and attendance")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Offender does not exist"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(value = "/offenders/crn/{crn}/contact-summary/inductions")
    public List<ContactSummary> getOffenderInitialAppointmentsByCrn(
        final @Parameter(name = "crn", description = "CRN of the offender", example = "X123456", required = true) @NotNull @PathVariable("crn") String crn,
        final @Parameter(name = "contactDateFrom", description = "Show contacts from this date", example = "2013-01-21") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "contactDateFrom", required = false) Optional<LocalDate> contactDateFrom)
        {
            final var contactFilter = ContactFilter.builder()
                .contactTypes(Optional.of(INITIAL_APPOINTMENT_CONTACT_TYPES))
                .contactDateFrom(contactDateFrom)
                .build();

            return offenderService.offenderIdOfCrn(crn)
                .map(offenderId -> contactService.contactSummariesFor(offenderId, contactFilter))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN '%s' does not exist", crn)));
        }

    @Operation(description = "Returns the contact summaries for an offender by CRN. Note: this endpoint is *not* paged.", tags = "Contact and attendance")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Offender does not exist"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(value = "/offenders/crn/{crn}/contact-summary")
    public Page<ContactSummary> getOffenderContactSummariesByCrn(
        final @Parameter(name = "crn", description = "CRN of the offender", example = "X123456", required = true) @NotNull @PathVariable("crn") String crn,
        final @RequestParam(value = "contactTypes", required = false) Optional<List<String>> contactTypes,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "from", required = false) Optional<LocalDateTime> from,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "to", required = false) Optional<LocalDateTime> to,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "contactDateFrom", required = false) Optional<LocalDate> contactDateFrom,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "contactDateTo", required = false) Optional<LocalDate> contactDateTo,
        final @RequestParam(value = "appointmentsOnly", required = false) Optional<Boolean> appointmentsOnly,
        final @RequestParam(value = "convictionId", required = false) Optional<Long> convictionId,
        final @RequestParam(value = "attended", required = false) Optional<Boolean> attended,
        final @RequestParam(value = "complied", required = false) Optional<Boolean> complied,
        final @RequestParam(value = "nationalStandard", required = false) Optional<Boolean> nationalStandard,
        final @RequestParam(value = "outcome", required = false) Optional<Boolean> outcome,
        final @Parameter(name = "include", description = "Contacts to include. Can be a contact type code, prefixed with 'type_' or appointments", example = "type_ccmp") @RequestParam(value = "include", required = false) Optional<List<String>> include,
        final @Parameter(name = "rarActivity", description = "Counts toward the RAR day calculation. If this filter is set to false then no filter will be applied.", example = "true")
        @RequestParam(value = "rarActivity", required = false) Optional<Boolean> rarActivity) {

        final var contactFilter = ContactFilter.builder()
            .contactTypes(contactTypes)
            .from(from)
            .to(to)
            .contactDateFrom(contactDateFrom)
            .contactDateTo(contactDateTo)
            .appointmentsOnly(appointmentsOnly)
            .convictionId(convictionId)
            .attended(attended)
            .complied(complied)
            .nationalStandard(nationalStandard)
            .outcome(outcome)
            .include(include)
            .rarActivity(rarActivity)
            .build();

        return offenderService.offenderIdOfCrn(crn)
            .map(offenderId -> contactService.contactSummariesFor(offenderId, contactFilter))
            .map(PageImpl::new)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN '%s' does not exist", crn)));
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

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/prisonOffenderManager", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Staff id does belong to the probation area related prison institution"),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(responseCode = "404", description = "The offender or prison institution is not found")
    })
    @Operation(description = "Allocates the prison offender manager for an offender in custody. This operation may also have a side affect of creating a Staff member " +
            "if one matching the name does not already exist. An existing staff member can be used if the staff id is supplied. Requires role ROLE_COMMUNITY_CUSTODY_UPDATE", tags = "Offender managers")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public CommunityOrPrisonOffenderManager allocatePrisonOffenderManagerByNomsNumber(final @PathVariable String nomsNumber,
                                                                                      final @RequestBody CreatePrisonOffenderManager prisonOffenderManager) {
        prisonOffenderManager.validate().ifPresent((errorMessage) -> {
            throw new InvalidAllocatePOMRequestException(prisonOffenderManager, errorMessage);
        });

        final var newPrisonOffenderManager =  Optional.ofNullable(prisonOffenderManager.getStaffId())
                .map(staffId -> offenderManagerService.allocatePrisonOffenderManagerByStaffId(nomsNumber, staffId, prisonOffenderManager))
                .orElseGet(() -> offenderManagerService.allocatePrisonOffenderManagerByName(nomsNumber, prisonOffenderManager))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with noms number %s not found", nomsNumber)));
        custodyService.updateCustodyPrisonLocation(nomsNumber, prisonOffenderManager.getNomsPrisonInstitutionCode());
        return newPrisonOffenderManager;
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/prisonOffenderManager", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "The noms number must be passed in the URL"),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(responseCode = "404", description = "The offender is not found"),
            @ApiResponse(responseCode = "409", description = "The offender does not have a POM to deallocate or the offender has multiple active noms numbers")
    })
    @Operation(description = "Deallocates the prison offender manager for an offender in custody. The POM is set back to its unallocated state. Requires role ROLE_COMMUNITY_CUSTODY_UPDATE", tags = "Offender managers")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public void deallocatePrisonOffenderManagerByNomsNumber(final @PathVariable String nomsNumber) {
        offenderManagerService.deallocatePrisonerOffenderManager(nomsNumber);
    }

    public static class InvalidAllocatePOMRequestException extends BadRequestException {
        InvalidAllocatePOMRequestException(final CreatePrisonOffenderManager createPrisonOffenderManager, final String message) {
            super(message);
        }
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

    @Operation(description = "Return the assessments for a CRN", tags = {"Assessments"})
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "The offender CRN is not found"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(path = "/offenders/crn/{crn}/assessments")
    public OffenderAssessments getAssessmentsByCrn(
        @Parameter(description = "CRN for the offender", example = "A123456", required = true)
        @NotNull @PathVariable(value = "crn") final String crn) {
        return assessmentService.getAssessments(crn)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
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

    @Operation(description = "Return all the recall NSIs for the noms number, active convictions only when licence has not expired", tags = "Sentence requirements and breach")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "404", description = "The offender NOMS number is not found")
        })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/convictions/active/nsis/recall")
    public NsiWrapper getRecallNsisForOffenderByNomsNumberAndActiveConvictions(
        @Parameter(name = "nomsNumber", description = "NOMS number for the offender", example = "A1234GH", required = true)
        @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
            .map(nsiService::getNonExpiredRecallNsiForOffenderActiveConvictions)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with nomsNumber %s not found", nomsNumber)));
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

    @Operation(description = "Reveals if the logged on user can access details about the supplied offender", tags = "Authentication and users")
    @RequestMapping(value = "/offenders/crn/{crn}/userAccess", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "User is restricted from access to offender"),
            @ApiResponse(responseCode = "404", description = "No such offender, or no such User (see body for detail)")
    })
    @PreAuthorize("hasAnyRole('ROLE_COMMUNITY', 'ROLE_PROBATION')")
    public ResponseEntity<AccessLimitation> checkUserAccessByCrn(
            final @PathVariable("crn") String crn) {
        return offenderService.getOffenderAccessLimitationsByCrn(crn)
            .map(this::accessLimitationResponseEntityOf)
            .orElse(new ResponseEntity<>(NOT_FOUND));
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

    @RequestMapping(value = "/offenders/crn/{crn}/probationStatus", method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "The offender was not found")
    })
    @Operation(description = "Returns the probation status for the given crn", tags = "-- Popular core APIs --")

    public ProbationStatusDetail getOffenderProbationStatusByCrn(final @PathVariable("crn") String crn) {
        return convictionService.probationStatusFor(crn)
            .orElseThrow(() -> new NotFoundException("Offender not found"));
    }

    @Operation(description = "Gets all offender personal contacts by CRN")
    @GetMapping(path = "/offenders/crn/{crn}/personalContacts")
    public List<PersonalContact> getAllOffenderPersonalContactsByCrn(
        @Parameter(name = "crn", description = "CRN of the offender", example = "X320741", required = true)
        @NotNull
        @PathVariable(value = "crn") final String crn) {
        return offenderService.getOffenderPersonalContactsByCrn(crn);
    }

    private ResponseEntity<AccessLimitation> accessLimitationResponseEntityOf(final OffenderAccessLimitations offender) {
        return accessLimitationResponseEntityOf(offender, currentUserSupplier.username().orElseThrow());
    }

    private ResponseEntity<AccessLimitation> accessLimitationResponseEntityOf(final OffenderAccessLimitations offender, final String username) {
        final var accessLimitation = userService.accessLimitationOf(username, offender);
        return new ResponseEntity<>(accessLimitation, (accessLimitation.isUserExcluded() || accessLimitation.isUserRestricted()) ? FORBIDDEN : OK);
    }
}
