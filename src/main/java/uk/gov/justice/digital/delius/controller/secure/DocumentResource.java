package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;
import uk.gov.justice.digital.delius.data.filters.DocumentFilter;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.DocumentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

@Tag(name = "Documents", description = "Requires ROLE_COMMUNITY")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class DocumentResource {
    private static final String typeDocumentation = "" +
        "<div>Supported types are " +
        "<ul>" +
        "<li>OFFENDER_DOCUMENT</li>" +
        "<li>CONVICTION_DOCUMENT</li>" +
        "<li>CPSPACK_DOCUMENT</li>" +
        "<li>PRECONS_DOCUMENT</li>" +
        "<li>COURT_REPORT_DOCUMENT</li>" +
        "<li>INSTITUTION_REPORT_DOCUMENT</li>" +
        "<li>ADDRESS_ASSESSMENT_DOCUMENT</li>" +
        "<li>APPROVED_PREMISES_REFERRAL_DOCUMENT</li>" +
        "<li>ASSESSMENT_DOCUMENT</li>" +
        "<li>CASE_ALLOCATION_DOCUMENT</li>" +
        "<li>PERSONAL_CONTACT_DOCUMENT</li>" +
        "<li>REFERRAL_DOCUMENT</li>" +
        "<li>NSI_DOCUMENT</li>" +
        "<li>PERSONAL_CIRCUMSTANCE_DOCUMENT</li>" +
        "<li>UPW_APPOINTMENT_DOCUMENT</li>" +
        "<li>CONTACT_DOCUMENT</li>" +
        "</ul>" +
        "</div>";

    private static final String subtypeDocumentation = "" +
        "<div>Supported sub-types are " +
        "<ul>" +
        "   <li>for type <b>COURT_REPORT_DOCUMENT</b>" +
        "       <ul>" +
        "           <li><b>PSR</b> - for Pre-Sentence Reports</li>" +
        "       </ul>" +
        "   </li>" +
        "</ul>" +
        "</div>";


    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;
    private final DocumentService documentService;

    @Operation(description = "Returns all documents' meta data for an offender by CRN", tags = "Documents")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Not Found. For example if the CRN is not known."),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(path = "/offenders/crn/{crn}/documents/grouped")
    public OffenderDocuments getOffenderDocumentsByCrn(
        @Parameter(name = "crn", description = "CRN for the offender", example = "X340906", required = true)
        @NotNull
        @PathVariable(value = "crn") final String crn,
        @Parameter(name = "type", description = "Optional filter for type" + typeDocumentation, example = "COURT_REPORT_DOCUMENT")
        @RequestParam(required = false) final String type,
        @Parameter(name = "subtype", description = "Optional filter for subtype within a type. Can only be used if type is also present" + subtypeDocumentation, example = "PSR")
        @RequestParam(required = false) final String subtype) {

        return offenderService.offenderIdOfCrn(crn)
            .map(offenderId -> documentService.offenderDocumentsFor(offenderId, DocumentFilter.of(type, subtype).getOrElseThrow(BadRequestException::new)))
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @Operation(description = "Returns the document for a given document id associated with an offender", tags = "Documents")
    @ApiResponse(responseCode = "200",
        description = "Returns the binary document data with an encoded filename in the content disposition header. ")
    @GetMapping(value = "/offenders/crn/{crn}/documents/{documentId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<Resource> getOffenderDocumentByCrn(
        @Parameter(name = "crn", description = "CRN for the offender", example = "X12345", required = true) @NotNull final @PathVariable("crn") String crn,
        @Parameter(name = "documentId", description = "Document Id", example = "12312322", required = true) @NotNull final @PathVariable("documentId") String documentId) {

        return Optional.ofNullable(alfrescoService.getDocument(documentId, crn))
            .orElseThrow(() -> new NotFoundException(String.format("document with id %s not found", documentId)));
    }

}
