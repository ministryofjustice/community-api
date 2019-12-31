package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.ReferenceDataService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Api(tags = "Reference Data API (Secure)", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class ReferenceDataResource {

    private final ReferenceDataService referenceDataService;

    @GetMapping("/probationAreas")
    public Page<KeyValue> getProbationAreaCodes(final @RequestParam(name = "active", required = false) boolean restrictActive) {
        log.info("Call to getProbationAreaCodes");
        return referenceDataService.getProbationAreasCodes(restrictActive);
    }

    @GetMapping(value = "/probationAreas/code/{code}/localDeliveryUnits")
    public Page<KeyValue> getLdusForProbationCode(final @PathVariable String code) {
        log.info("Call to getLdusForProbationCode");
        final var ldus = referenceDataService.getLocalDeliveryUnitsForProbationArea(code);

        if (ldus.isEmpty()) {
            throw new NotFoundException(String.format("Probation area with code %s", code));
        }
        return ldus;
    }
}
