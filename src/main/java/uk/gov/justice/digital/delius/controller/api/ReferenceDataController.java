package uk.gov.justice.digital.delius.controller.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.ReferenceDataService;

import java.util.List;

@RestController
@Slf4j
@Tag(description = "Reference Data APIs", name = "Reference data")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @Autowired
    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @RequestMapping(value = "/probationAreas/code/{code}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<ProbationArea>> getProbationAreasForCode(final @RequestHeader HttpHeaders httpHeaders,
                                                                        final @RequestParam(name = "active", required = false) boolean restrictActive,
                                                                        final @PathVariable String code) {
        log.info("Call to getProbationAreasForCode");
        List<ProbationArea> probationAreasForCode = referenceDataService.getProbationAreasForCode(code, restrictActive);

        if (probationAreasForCode.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(probationAreasForCode, HttpStatus.OK);
    }

}
