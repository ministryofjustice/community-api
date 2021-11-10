package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.service.DeliusDocumentsService;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class DeliusDocumentsController {

    private DeliusDocumentsService deliusDocumentsService;

    @PostMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(
        value = {
            @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_PROBATION"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Active Offender Manager could not be found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public UploadedDocumentCreateResponse createUPWDocumentInDelius(
        @RequestPart MultipartFile fileData,
        @PathVariable String crn,
        @PathVariable Long convictionId
    ) {
        return deliusDocumentsService.createUPWDocument(crn, convictionId, fileData);
    }
}
