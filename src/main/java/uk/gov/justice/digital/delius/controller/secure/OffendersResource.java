package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.DocumentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Api(description = "Offender resources protected by OAUTH2", tags = "Offenders (Secure)")
@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class OffendersResource {

    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;
    private final DocumentService documentService;
    private final ConvictionService convictionService;

    @ApiOperation(
            value = "Return the responsible officer (RO) for an offender",
            notes = "Accepts a NOMIS offender nomsNumber in the format A9999AA",
            authorizations = {@Authorization("ROLE_COMMUNITY")},
            nickname = "getResponsibleOfficersForOffender")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = ResponsibleOfficer.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/responsibleOfficers")
    @NationalUserOverride
    public ResponseEntity<List<ResponsibleOfficer>> getResponsibleOfficersForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true) @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
            @ApiParam(name = "current", value = "Current only", example = "false", required = false) @RequestParam(name = "current", required = false, defaultValue = "false") final boolean current) {
        return offenderService.getResponsibleOfficersForNomsNumber(nomsNumber, current)
                .map(responsibleOfficer -> new ResponseEntity<>(responsibleOfficer, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiOperation(
            value = "Return the convictions (AKA Delius Event) for an offender",
            notes = "See http://deliusapi-dev.sbw4jt6rsq.eu-west-2.elasticbeanstalk.com/api/swagger-ui.html#!/Offender32convictions/getOffenderConvictionsByNomsNumberUsingGET for further details",
            authorizations = {@Authorization("ROLE_COMMUNITY")},
            nickname = "getConvictionsForOffender")
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/convictions")
    @NationalUserOverride
    public ResponseEntity<List<Conviction>> getConvictionsForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
                .map(offenderId -> new ResponseEntity<>(convictionService.convictionsFor(offenderId), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(
            value = "Return the details for an offender",
            notes = "See http://deliusapi-dev.sbw4jt6rsq.eu-west-2.elasticbeanstalk.com/api/swagger-ui.html#!/Offenders/getOffenderSummaryByNomsNumberUsingGET",
            authorizations = {@Authorization("ROLE_COMMUNITY")},
            nickname = "getOffenderDetails")
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}")
    @NationalUserOverride
    public ResponseEntity<OffenderDetailSummary> getOffenderDetails(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {
        Optional<OffenderDetailSummary> offender = offenderService.getOffenderSummaryByNomsNumber(nomsNumber);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(new ResponseEntity<>(OffenderDetailSummary.builder().build(), NOT_FOUND));
    }

    @ApiOperation(
            value = "Returns all document's meta data for an offender",
            notes = "See http://deliusapi-dev.sbw4jt6rsq.eu-west-2.elasticbeanstalk.com/api/swagger-ui.html#!/Offenders/getOffenderGroupedDocumentByNomsNumber",
            authorizations = {@Authorization("ROLE_COMMUNITY")},
            nickname = "getOffenderDetails")
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/documents/grouped")
    @NationalUserOverride
    public ResponseEntity<OffenderDocuments> getOffenderDocuments(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
                .map(offenderId -> new ResponseEntity<>(documentService.offenderDocumentsFor(offenderId), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(
            value = "Returns the document contents meta data for a given document associated with an offender",
            notes = "See http://deliusapi-dev.sbw4jt6rsq.eu-west-2.elasticbeanstalk.com/api/swagger-ui.html#!/Offenders/getOffenderDocumentByOffenderIdUsingGET",
            authorizations = {@Authorization("ROLE_COMMUNITY")},
            nickname = "getOffenderDocument")
    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents/{documentId}", method = RequestMethod.GET)
    @NationalUserOverride
    public HttpEntity<Resource> getOffenderDocument(
            final @PathVariable("nomsNumber") String nomsNumber,
            final @PathVariable("documentId") String documentId) {

        return offenderService.crnOf(nomsNumber)
                .map(crn -> alfrescoService.getDocument(documentId, crn))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

}

