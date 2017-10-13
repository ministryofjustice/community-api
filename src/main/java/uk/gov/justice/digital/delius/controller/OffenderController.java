package uk.gov.justice.digital.delius.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.service.OffenderService;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("${base.url:/delius}")
@Log
public class OffenderController {

    private final OffenderService offenderService;

    @Autowired
    public OffenderController(OffenderService offenderService) {
        this.offenderService = offenderService;
    }

    @RequestMapping(value = "/offenders/{offenderId}", method = RequestMethod.GET)
    public ResponseEntity<OffenderDetail> getOffender(final @PathVariable("offenderId") Long offenderId) {

        return offenderService.getOffender(offenderId).map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)
        ).orElse(notFound());
    }

    private ResponseEntity<OffenderDetail> notFound() {
        return new ResponseEntity<>(NOT_FOUND);
    }
}
