package uk.gov.justice.digital.delius.controller.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Tag(name = "Offenders resources")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class OffenderController {

    private final OffenderService offenderService;

    @Autowired
    public OffenderController(OffenderService offenderService) {
        this.offenderService = offenderService;
    }

    @RequestMapping(value = "/offenders/crn/{crn}/all", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetail> getFullFatOffenderByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("crn") String crn) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByCrn(crn);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailNotFound());

    }

    private ResponseEntity<OffenderDetail> offenderDetailNotFound() {
        return new ResponseEntity<>(OffenderDetail.builder().build(), NOT_FOUND);
    }
}
