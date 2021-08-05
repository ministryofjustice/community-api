package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Contact;
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
import uk.gov.justice.digital.delius.data.api.PrimaryIdentifiers;
import uk.gov.justice.digital.delius.data.api.ProbationStatusDetail;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficerSwitch;
import uk.gov.justice.digital.delius.data.api.SentenceStatus;
import uk.gov.justice.digital.delius.data.filters.OffenderFilter;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.service.AssessmentService;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.TierService;
import uk.gov.justice.digital.delius.service.UserAccessService;
import uk.gov.justice.digital.delius.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory.REHABILITATION_ACTIVITY_REQUIREMENT_CODE;

@Api(tags = "Core offender", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@Validated
public class OffendersResource {

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
    private final TierService tierService;

    @ApiOperation(
            value = "Return the responsible officer (RO) for an offender",
            notes = "Accepts a NOMIS offender nomsNumber in the format A9999AA",
            tags = "Offender managers")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/responsibleOfficers")
    public ResponseEntity<List<ResponsibleOfficer>> getResponsibleOfficersForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true) @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
            @ApiParam(name = "current", value = "Current only", example = "false") @RequestParam(name = "current", required = false, defaultValue = "false") final boolean current) {
        return offenderService.getResponsibleOfficersForNomsNumber(nomsNumber, current)
                .map(responsibleOfficer -> new ResponseEntity<>(responsibleOfficer, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiOperation(
            value = "Returns the current community and prison offender managers for an offender",
            notes = "Accepts a NOMIS offender nomsNumber in the format A9999AA",
            tags = {"Offender managers", "-- Popular core APIs --"})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/allOffenderManagers")
    public List<CommunityOrPrisonOffenderManager> getAllOffenderManagersForOffender(
        @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
        @NotNull
        @PathVariable(value = "nomsNumber") final String nomsNumber,
        @ApiParam(name = "includeProbationAreaTeams", value = "include teams on the ProbationArea records", example = "true")
        @RequestParam(name = "includeProbationAreaTeams", required = false, defaultValue = "false") final boolean includeProbationAreaTeams) {
        return offenderManagerService.getAllOffenderManagersForNomsNumber(nomsNumber, includeProbationAreaTeams)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with NOMS number %s not found", nomsNumber)));
    }

    @ApiOperation(
        value = "Returns the current community and prison offender managers for an offender",
        notes = "Accepts an offender CRN in the format A999999",
        tags = {"Offender managers", "-- Popular core APIs --"})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path = "/offenders/crn/{crn}/allOffenderManagers")
    public List<CommunityOrPrisonOffenderManager> getAllOffenderManagersForOffenderbyCrn(
        @ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true)
        @NotNull
        @PathVariable(value = "crn") final String crn,
        @ApiParam(name = "includeProbationAreaTeams", value = "include teams on the ProbationArea records", example = "true")
        @RequestParam(name = "includeProbationAreaTeams", required = false, defaultValue = "false") final boolean includeProbationAreaTeams) {
        return offenderManagerService.getAllOffenderManagersForCrn(crn, includeProbationAreaTeams)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN %s not found", crn)));
    }

    @ApiOperation(value = "Return the convictions (AKA Delius Event) for an offender", tags = {"Convictions","-- Popular core APIs --"})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 409, message = "Multiple offenders found in the same state ", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/convictions")
    public List<Conviction> getConvictionsForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
            @ApiParam(name = "activeOnly", value = "retrieve only active convictions", example = "true")
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly,
            @ApiParam(name = "failOnDuplicate", value = "Should fail if multiple offenders found regardless of status", example = "true", defaultValue = "false")
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

    @ApiOperation(value = "Return the details for an offender. If multiple offenders found the active one wll be returned", tags = "-- Popular core APIs --")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Multiple offenders found in the same state ", response = ErrorResponse.class)
        })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}")
    public OffenderDetailSummary getOffenderDetails(
        @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
        @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
        @ApiParam(name = "failOnDuplicate", value = "Should fail if multiple offenders found regardless of status", example = "true", defaultValue = "false")
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

    @ApiOperation(value = "Returns the contact details for an offender by NOMS number", tags = "Contact and attendance")
    @ApiResponses(
            value = {
                @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                @ApiResponse(code = 404, message = "Offender does not exist"),
                @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/offenders/nomsNumber/{nomsNumber}/contacts")
    public ResponseEntity<List<Contact>> getOffenderContactReportByNomsNumber(@ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true) @NotNull final @PathVariable("nomsNumber") String nomsNumber,
                                                                              final @RequestParam(value = "contactTypes", required = false) Optional<List<String>> contactTypes,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam(value = "from", required = false) Optional<LocalDateTime> from,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam(value = "to", required = false) Optional<LocalDateTime> to) {
        final var contactFilter = ContactFilter.builder()
                .contactTypes(contactTypes)
                .from(from)
                .to(to)
                .build();

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
                .map(offenderId -> new ResponseEntity<>(contactService.contactsFor(offenderId, contactFilter), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Returns the contact summaries for an offender by CRN", tags = "Contact and attendance")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender does not exist"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/offenders/crn/{crn}/contact-summary")
    public Page<ContactSummary> getOffenderContactSummariesByCrn(
        final @ApiParam(name = "crn", value = "CRN of the offender", example = "X123456", required = true) @NotNull @PathVariable("crn") String crn,
        final @ApiParam(name = "page", value = "Page number (0-based)", example = "0") @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int page,
        final @ApiParam(name = "pageSize", value = "Optional size of page", example = "20") @RequestParam(required = false, defaultValue = "1000") @Positive int pageSize,
        final @RequestParam(value = "contactTypes", required = false) Optional<List<String>> contactTypes,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "from", required = false) Optional<LocalDateTime> from,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "to", required = false) Optional<LocalDateTime> to,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "contactDateFrom", required = false) Optional<LocalDate> contactDateFrom,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "contactDateTo", required = false) Optional<LocalDate> contactDateTo,
        final @RequestParam(value = "appointmentsOnly", required = false) Optional<Boolean> appointmentsOnly,
        final @RequestParam(value = "convictionId", required = false) Optional<Long> convictionId,
        final @RequestParam(value = "attended", required = false) Optional<Boolean> attended,
        final @RequestParam(value = "complied", required = false) Optional<Boolean> complied,
        final @RequestParam(value = "nationalStandard", required = false) Optional<Boolean> nationalStandard) {

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
            .build();

        return offenderService.offenderIdOfCrn(crn)
            .map(offenderId -> contactService.contactSummariesFor(offenderId, contactFilter, page, pageSize))
            .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN '%s' does not exist", crn)));
    }

    @ApiOperation(
            value = "Returns the latest recall and release details for an offender",
            notes = "Accepts a NOMIS offender nomsNumber in the format A9999AA",
            tags = "Convictions")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/release")
    public OffenderLatestRecall getLatestRecallAndReleaseForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
            @NotNull
            @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return getOffenderLatestRecall(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @ApiOperation(
            value = "Returns the latest recall and release details for an offender",
            notes = "Accepts an offender CRN in the format A999999",
            tags = "Convictions")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/release")
    public OffenderLatestRecall getLatestRecallAndReleaseForOffenderByCrn(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true)
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
            @ApiResponse(code = 400, message = "Staff id does belong to the probation area related prison institution"),
            @ApiResponse(code = 403, message = "Forbidden, requires ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 404, message = "The offender or prison institution is not found")
    })
    @ApiOperation(value = "Allocates the prison offender manager for an offender in custody. This operation may also have a side affect of creating a Staff member " +
            "if one matching the name does not already exist. An existing staff member can be used if the staff id is supplied.", notes = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE", tags = "Offender managers")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public CommunityOrPrisonOffenderManager allocatePrisonOffenderManagerByNomsNumber(final @PathVariable String nomsNumber,
                                                                                      final @RequestBody CreatePrisonOffenderManager prisonOffenderManager) {
        log.info("Request to allocate a prison offender manager to {} at prison with code {}", nomsNumber, prisonOffenderManager.getNomsPrisonInstitutionCode());

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
            @ApiResponse(code = 400, message = "The noms number must be passed in the URL"),
            @ApiResponse(code = 403, message = "Forbidden, requires ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 404, message = "The offender is not found"),
            @ApiResponse(code = 409, message = "The offender does not have a POM to deallocate or the offender has multiple active noms numbers")
    })
    @ApiOperation(value = "Deallocates the prison offender manager for an offender in custody. The POM is set back to its unallocated state", notes = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE", tags = "Offender managers")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public void deallocatePrisonOffenderManagerByNomsNumber(final @PathVariable String nomsNumber) {
        offenderManagerService.deallocatePrisonerOffenderManager(nomsNumber);
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/responsibleOfficer/switch", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Either set true for the prisoner offender manager or the community offender manager"),
            @ApiResponse(code = 403, message = "Forbidden, requires ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 404, message = "The offender is not found"),
            @ApiResponse(code = 409, message = "Cannot find a current RO for offender or Cannot find an active POM for offender or Cannot find an active COM for offender")
    })
    @ApiOperation(value = "Sets the responsible officer for an offender to either the current prison offender manager to community offender manager. This will allow the responsible officer to be set to an unallocated offender manager", notes = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE", tags = "Offender managers")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public CommunityOrPrisonOffenderManager switchResponsibleOfficer(final @PathVariable String nomsNumber,
                                                                                      final @Valid @RequestBody ResponsibleOfficerSwitch responsibleOfficerSwitch) {
        return offenderManagerService.switchResponsibleOfficer(nomsNumber, responsibleOfficerSwitch);
    }


    public static class InvalidAllocatePOMRequestException extends BadRequestException {
        InvalidAllocatePOMRequestException(final CreatePrisonOffenderManager createPrisonOffenderManager, final String message) {
            super(message);
            log.warn("Bad request: {}", createPrisonOffenderManager);
        }
    }

    @RequestMapping(value = "/offenders/crn/{crn}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
            @ApiResponse(code = 404, message = "The offender not found")
    })
    @ApiOperation(value = "Returns the offender summary for the given crn", tags = "-- Popular core APIs --")

    public OffenderDetailSummary getOffenderSummaryByCrn(final @PathVariable("crn") String crn, Authentication authentication) {
        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());

        return offenderService.getOffenderSummaryByCrn(crn)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/all", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "The offender is not found"),
            @ApiResponse(code = 403, message = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
    })
    @ApiOperation(value = "Returns the full offender detail for the given crn", tags = "-- Popular core APIs --")
    public OffenderDetail getOffenderDetailByCrn(final @PathVariable("crn") String crn, Authentication authentication) {
        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());

        final var offender = offenderService.getOffenderByCrn(crn);
        return offender.orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/all", method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "The offender is not found"),
        @ApiResponse(code = 409, message = "Multiple offenders found in the same state", response = ErrorResponse.class)
    })
    @ApiOperation(value = "Returns the full offender detail for the given nomsNumber. If multiple offender found the active one will be returned", tags = "-- Popular core APIs --")
    public OffenderDetail getOffenderDetailByNomsNumber(
        @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
        @NotNull @PathVariable(value = "nomsNumber")
        final String nomsNumber,
        @ApiParam(name = "failOnDuplicate", value = "Should fail if multiple offenders found", example = "true", defaultValue = "false")
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

    @ApiOperation(value = "Return the convictions (AKA Delius Event) for an offender", tags = {"-- Popular core APIs --", "Convictions"})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "The offender is not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions")
    public List<Conviction> getConvictionsForOffenderByCrn(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @ApiParam(name = "activeOnly", value = "retrieve only active convictions", example = "true")
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly) {

        return offenderService.offenderIdOfCrn(crn)
                .map(offenderId -> convictionService.convictionsFor(offenderId, activeOnly))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @ApiOperation(value = "Return the convictions (AKA Delius Event) for an offender that contain RAR", tags = {"-- Popular core APIs --", "Convictions"})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/offenders/crn/{crn}/convictions-with-rar")
    public List<Conviction> getOffenderConvictionsWithRarByCrn(
        @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
        @NotNull @PathVariable(value = "crn") final String crn) {

        return offenderService.offenderIdOfCrn(crn)
                .map(offenderId -> convictionService.convictionsWithActiveRequirementFor(offenderId, REHABILITATION_ACTIVITY_REQUIREMENT_CODE))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @ApiOperation(value = "Return the conviction (AKA Delius Event) for a conviction ID and a CRN", tags = {"-- Popular core APIs --", "Convictions"})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "The offender CRN or conviction ID is not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}")
    public Conviction getConvictionForOffenderByCrnAndConvictionId(
            @ApiParam(value = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @ApiParam(value = "ID for the conviction / event", example = "2500295345", required = true)
            @NotNull @PathVariable(value = "convictionId") final Long convictionId) {

        return offenderService.offenderIdOfCrn(crn)
                .map((offenderId) -> convictionService.convictionFor(offenderId, convictionId))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)))
                .orElseThrow(() -> new NotFoundException(String.format("Conviction with ID %s for Offender with crn %s not found", convictionId, crn)));
    }

    @ApiOperation(value = "Return the assessments for a CRN", tags = {"Assessments"})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender CRN is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path = "/offenders/crn/{crn}/assessments")
    public OffenderAssessments getAssessmentsByCrn(
        @ApiParam(value = "CRN for the offender", example = "A123456", required = true)
        @NotNull @PathVariable(value = "crn") final String crn) {
        return assessmentService.getAssessments(crn)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @ApiOperation(value = "Update offender tier. Requires ROLE_MANAGEMENT_TIER_UPDATE", tags = {"Assessments"})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden, requires ROLE_MANAGEMENT_TIER_UPDATE"),
            @ApiResponse(code = 404, message = "The offender CRN or Tier is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @PostMapping(path = "/offenders/crn/{crn}/tier/{tier}")
    @PreAuthorize("hasRole('ROLE_MANAGEMENT_TIER_UPDATE')")
    public void updateTier(
        @ApiParam(value = "CRN for the offender", example = "A123456", required = true)
        @NotNull @PathVariable(value = "crn") final String crn,
        @ApiParam(value = "New tier", example = "A1", required = true, allowableValues="A0, A1, A2, A3, B0, B1, B2, B3, C0, C1, C2, C3, D0, D1, D2, D3")
        @NotNull @PathVariable(value = "tier") final String tier) {
        tierService.updateTier(crn,tier);
    }

    @ApiOperation(value = "Return the NSIs for a conviction ID and a CRN, filtering by NSI codes", tags = "Sentence requirements and breach")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "The offender CRN is not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/nsis")
    public NsiWrapper getNsiForOffenderByCrnAndConvictionId(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @ApiParam(name = "convictionId", value = "ID for the conviction / event", example = "2500295345", required = true)
            @NotNull @PathVariable(value = "convictionId") final Long convictionId,
            @ApiParam(name = "nsiCodes", value = "list of NSI codes to constrain by", example = "BRE,BRES", required = true)
            @NotEmpty @RequestParam(value = "nsiCodes") final List<String> nsiCodes) {

        return offenderService.offenderIdOfCrn(crn)
                .map((offenderId) -> nsiService.getNsiByCodes(offenderId, convictionId, nsiCodes))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)))
                .orElseThrow(() -> new NotFoundException(String.format("Conviction with ID %s for Offender with crn %s not found", convictionId, crn)));
    }

    @ApiOperation(value = "Return all the NSIs for the CRN, active convictions only, filtering by NSI codes", tags = "Sentence requirements and breach")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "The offender CRN is not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/active/nsis")
    public NsiWrapper getNsisForOffenderByCrnAndActiveConvictions(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @ApiParam(name = "nsiCodes", value = "list of NSI codes to constrain by", example = "BRE,BRES", required = true)
            @NotEmpty @RequestParam(value = "nsiCodes") final List<String> nsiCodes) {

        return offenderService.offenderIdOfCrn(crn)
                .map((offenderId) -> nsiService.getNsiByCodesForOffenderActiveConvictions(offenderId, nsiCodes))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @ApiOperation(value = "Return all the recall NSIs for the noms number, active convictions only when licence has not expired", tags = "Sentence requirements and breach")
    @ApiResponses(
        value = {
            @ApiResponse(code = 404, message = "The offender NOMS number is not found", response = ErrorResponse.class)
        })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/convictions/active/nsis/recall")
    public NsiWrapper getRecallNsisForOffenderByNomsNumberAndActiveConvictions(
        @ApiParam(name = "nomsNumber", value = "NOMS number for the offender", example = "A1234GH", required = true)
        @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
            .map(nsiService::getNonExpiredRecallNsiForOffenderActiveConvictions)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with nomsNumber %s not found", nomsNumber)));
    }

    @ApiOperation(value = "Return an NSI by crn, convictionId and nsiId", tags = "Convictions")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "The offender CRN is not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/nsis/{nsiId}")
    public Nsi getNsiByNsiId(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn,
            @ApiParam(name = "convictionId", value = "ID for the conviction / event", example = "2500295345", required = true)
            @NotNull @PathVariable(value = "convictionId") final Long convictionId,
            @ApiParam(name = "nsiId", value = "ID for the nsi", example = "2500295123", required = true)
            @PathVariable(value = "nsiId") final Long nsiId) {
        return offenderService.getOffenderByCrn(crn)
                .map((offender) -> convictionService.convictionFor(offender.getOffenderId(), convictionId)
                    .orElseThrow(() -> new NotFoundException(String.format("Conviction with convictionId %s not found for offender with crn %s", convictionId, crn)))
                ).map(conviction -> nsiService.getNsiById(nsiId))
                .orElseThrow(() -> new NotFoundException(String.format("NSI with id %s not found", nsiId)))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @ApiOperation(value = "Return pageable list of all offender identifiers that match the supplied filter")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "java.lang.Integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "java.lang.Integer", paramType = "query",
                    value = "Number of records per page.", example = "10", defaultValue = "10"),
            @ApiImplicitParam(name = "sort",dataType = "java.lang.String", paramType = "query", example = "crn,desc", defaultValue = "crn,asc",
                    value = "Sort column and direction. Multiple sort params allowed.")})
    @GetMapping(value = "/offenders/primaryIdentifiers")
    public Page<PrimaryIdentifiers> getOffenderIds(
            @ApiParam(value = "Optionally specify an offender filter") final OffenderFilter filter,
            @PageableDefault(sort = {"offenderId"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return offenderService.getAllPrimaryIdentifiers(filter, pageable);
    }

    @ApiOperation(value = "Return sentence and custodial status information by crn, convictionId.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender CRN / conviction ID is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/sentenceStatus")
    public Optional<SentenceStatus> getSentenceStatusByConvictionId(
        @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
        @PathVariable(value = "crn") final String crn,
        @ApiParam(name = "convictionId", value = "ID for the conviction / event", example = "2500295345", required = true)
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

    @ApiIgnore("This endpoint is too specific to use case and does not reflect best practice so is deprecated for new use")
    @Deprecated
    @ApiOperation(value = "Return sentence and custodial status information by crn, convictionId and sentenceId.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "The offender CRN / conviction ID / sentence ID is not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/sentences/{sentenceId}/status")
    public SentenceStatus getSentenceStatusBySentenceId(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
            @PathVariable(value = "crn") final String crn,
            @ApiParam(name = "convictionId", value = "ID for the conviction / event", example = "2500295345", required = true)
            @PathVariable(value = "convictionId") final Long convictionId,
            @ApiParam(name = "sentenceId", value = "ID for the sentence", example = "2500295123", required = true)
            @PathVariable(value = "sentenceId") final Long sentenceId) {

        return sentenceService.getSentenceStatus(crn, convictionId, sentenceId)
                .orElseThrow(() -> new NotFoundException(String.format("Sentence not found for crn '%s', convictionId '%s', and sentenceId '%s'", crn, convictionId, sentenceId)));
    }

    @ApiOperation(value = "Reveals if the logged on user can access details about the supplied offender", tags = "Authentication and users")
    @RequestMapping(value = "/offenders/crn/{crn}/userAccess", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "User is restricted from access to offender", response = AccessLimitation.class),
            @ApiResponse(code = 404, message = "No such offender, or no such User (see body for detail)")
    })
    @PreAuthorize("hasAnyRole('ROLE_COMMUNITY', 'ROLE_PROBATION')")
    public ResponseEntity<AccessLimitation> checkUserAccessByCrn(
            final @PathVariable("crn") String crn) {
        return offenderService.getOffenderByCrn(crn)
            .map(this::accessLimitationResponseEntityOf)
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiOperation(value = "Reveals if the specified user can access details about the supplied offender", tags = "Authentication and users")
    @RequestMapping(value = "/offenders/crn/{crn}/user/{username}/userAccess", method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "User is restricted from access to offender", response = AccessLimitation.class),
        @ApiResponse(code = 404, message = "No such offender, or no such User (see body for detail)")
    })
    public ResponseEntity<AccessLimitation> checkUserAccessByCrn(
        final @PathVariable("crn") String crn,
        final @PathVariable("username") String username) {
        return offenderService.getOffenderByCrn(crn)
            .map(offender -> accessLimitationResponseEntityOf(offender, username))
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/probationStatus", method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "The offender was not found")
    })
    @ApiOperation(value = "Returns the probation status for the given crn", tags = "-- Popular core APIs --")

    public ProbationStatusDetail getOffenderProbationStatusByCrn(final @PathVariable("crn") String crn) {
        return convictionService.probationStatusFor(crn)
            .orElseThrow(() -> new NotFoundException("Offender not found"));
    }

    @ApiOperation(value = "Gets all offender personal contacts by CRN")
    @GetMapping(path = "/offenders/crn/{crn}/personalContacts")
    public List<PersonalContact> getAllOffenderPersonalContactsByCrn(
        @ApiParam(name = "crn", value = "CRN of the offender", example = "X320741", required = true)
        @NotNull
        @PathVariable(value = "crn") final String crn) {
        return offenderService.getOffenderPersonalContactsByCrn(crn);
    }

    private ResponseEntity<AccessLimitation> accessLimitationResponseEntityOf(final OffenderDetail offender) {
        return accessLimitationResponseEntityOf(offender, currentUserSupplier.username().orElseThrow());
    }

    private ResponseEntity<AccessLimitation> accessLimitationResponseEntityOf(final OffenderDetail offender, final String username) {
        final var accessLimitation = userService.accessLimitationOf(username, offender);
        return new ResponseEntity<>(accessLimitation, (accessLimitation.isUserExcluded() || accessLimitation.isUserRestricted()) ? FORBIDDEN : OK);
    }
}
