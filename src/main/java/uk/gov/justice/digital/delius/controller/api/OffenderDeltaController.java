package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.OffenderDeltaService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@Api( description = "Low level API for propagating significant events", tags = "Offender deltas")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class OffenderDeltaController {

    private final OffenderDeltaService offenderDeltaService;

    @Autowired
    public OffenderDeltaController(OffenderDeltaService offenderDeltaService) {
        this.offenderDeltaService = offenderDeltaService;
    }

    @RequestMapping(value = "/offenderDeltaIds", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<OffenderDelta>> getOffenderDeltas(final @RequestHeader HttpHeaders httpHeader) {
        log.info("Call to getOffenderDeltas");
        return new ResponseEntity<>(offenderDeltaService.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/offenderDeltaIds", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @JwtValidation
    public void deleteOffenderDeltas(final @RequestHeader HttpHeaders httpHeaders,
                                     final @RequestParam("before")
                                     @NotNull
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                             LocalDateTime dateTime) {
        log.info("Call to deleteOffenderDeltas before {}", dateTime.toString());
        offenderDeltaService.deleteBefore(dateTime);
    }

}
