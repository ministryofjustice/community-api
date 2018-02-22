package uk.gov.justice.digital.delius.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.DocumentLink;
import uk.gov.justice.digital.delius.service.DocumentService;

@RestController
@Slf4j
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
