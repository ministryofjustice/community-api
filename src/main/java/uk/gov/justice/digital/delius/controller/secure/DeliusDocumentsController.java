package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.service.DeliusDocumentsService;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeliusDocumentsController {

    private DeliusDocumentsService deliusDocumentsService;
    private final String contactType = "EASU";

    @PostMapping(path = "/offender/{crn}/event/{eventId}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(
        value = {
            @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })

    public ResponseEntity<UploadedDocumentCreateResponse> createDocumentInDelius(
        @RequestPart MultipartFile file,
        @PathVariable String crn,
        @PathVariable Long eventId
    ){
        UploadedDocumentCreateResponse response = deliusDocumentsService.createDocument(crn, eventId, contactType, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
