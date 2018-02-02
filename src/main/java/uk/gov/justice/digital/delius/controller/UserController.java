package uk.gov.justice.digital.delius.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.jwt.JwtValidation;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
public class UserController {

    private final Jwt jwt;
    private final UserRepository userRepository;

    @Autowired
    public UserController(Jwt jwt, UserRepository userRepository) {
        this.jwt = jwt;
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<User>> getOffenderByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                              final @RequestParam("surname") @NotNull String surname,
                                                              final @RequestParam("forename") Optional<String> forename) {
        return getUsersList(surname, forename);
    }

    @Transactional
    public ResponseEntity<List<User>> getUsersList(String surname, Optional<String> forename) {

        List<uk.gov.justice.digital.delius.data.api.User> users = forename.map(f -> userRepository.findBySurnameIgnoreCaseAndForenameIgnoreCase(surname, f))
                .orElse(userRepository.findBySurnameIgnoreCase(surname))
                .stream()
                .map(user -> uk.gov.justice.digital.delius.data.api.User.builder()
                        .distinguishedName(user.getDistinguishedName())
                        .endDate(user.getEndDate())
                        .externalProviderEmployeeFlag(user.getExternalProviderEmployeeFlag())
                        .externalProviderId(user.getExternalProviderId())
                        .forename(user.getForename())
                        .forename2(user.getForename2())
                        .surname(user.getSurname())
                        .organisationId(user.getOrganisationId())
                        .privateFlag(user.getPrivateFlag())
                        .scProviderId(user.getScProviderId())
                        .staffId(user.getStaffId())
                        .build()).collect(Collectors.toList());
        return new ResponseEntity<>(users, OK);
    }

}
