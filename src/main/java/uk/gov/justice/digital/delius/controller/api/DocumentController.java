package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.DocumentLink;
import uk.gov.justice.digital.delius.service.DocumentService;

@RestController
@Slf4j
@Api( description = "Offender document resources", tags = "Offender Documents")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RequestMapping(path = "/documentLink", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void insertDocument(final @RequestBody DocumentLink documentLink) {
        log.info("Received call to insertDocument with body {}", documentLink);

        documentService.insertDocument(documentLink);
    }

}
