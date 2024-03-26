package uk.gov.justice.digital.delius.controller.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.config.ApplicationInsightsConfiguration;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.national.entity.ProbationArea;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;
import uk.gov.justice.digital.delius.user.UserData;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/logon")
@Slf4j
@Tag(description = "Obtain JWT token", name = "Logon as user")
public class LogonController {
    private static final List<String> SYSTEM_USERS = List.of(CurrentUserSupplier.NATIONAL_USER, CurrentUserSupplier.API_USER);
    private final Jwt jwt;
    private final LdapRepository ldapRepository;
    private final UserRepositoryWrapper userRepositoryWrapper;

    @Autowired
    public LogonController(Jwt jwt, LdapRepository ldapRepository, UserRepositoryWrapper userRepositoryWrapper) {
        this.jwt = jwt;
        this.ldapRepository = ldapRepository;
        this.userRepositoryWrapper = userRepositoryWrapper;
    }

    private ResponseEntity<String> notFound() {
        return new ResponseEntity<>(NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "text/plain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User lookup: not found")
    })
    @Transactional
    public ResponseEntity<String> getToken(final @RequestBody String distinguishedName) {
        Optional<String> maybeUid = SYSTEM_USERS.contains(distinguishedName) ? Optional.of(distinguishedName) : ldapRepository.getDeliusUid(distinguishedName);

        maybeUid.ifPresent(ApplicationInsightsConfiguration::setNewTechClientId);
        return maybeUid.map(uid ->
                new ResponseEntity<>(jwt.buildToken(UserData.builder()
                        .distinguishedName(distinguishedName)
                        .uid(uid)
                        .probationAreaCodes(probationAreaCodesOf(uid))
                        .build()), HttpStatus.OK)).orElse(notFound());
    }

    private List<String> probationAreaCodesOf(String uid) {
        return Optional.ofNullable(
                userRepositoryWrapper
                        .getUser(uid)
                        .getProbationAreas())
                .map(probationAreas -> probationAreas.stream()
                        .map(ProbationArea::getCode)
                        .collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    @ExceptionHandler(InvalidNameException.class)
    public ResponseEntity<String> badDistinguishedName(Exception e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity<String> noSuchDistinguishedName(Exception e) {
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }
}
