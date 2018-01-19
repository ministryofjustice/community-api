package uk.gov.justice.digital.delius.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;
import uk.gov.justice.digital.delius.service.OffenderDeltaService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Log
public class OffenderDeltaController {

    private final OffenderDeltaService offenderDeltaService;

    @Autowired
    public OffenderDeltaController(OffenderDeltaService offenderDeltaService) {
        this.offenderDeltaService = offenderDeltaService;
    }

    @RequestMapping(value = "/offenderDeltaIds", method = RequestMethod.GET)
    public ResponseEntity<List<OffenderDelta>> getOffenderDeltas() {

        return new ResponseEntity<>(offenderDeltaService.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/offenderDeltaIds", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteOffenderDeltas(@RequestParam("before")
                                     @NotNull
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                             LocalDateTime dateTime) {
        offenderDeltaService.deleteBefore(dateTime);
    }

}
