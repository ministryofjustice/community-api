package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.service.UserService;

@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Tag(name = "Users")
@Validated
public class UserController {

    private final UserService userService;

    @Operation(description = "Find user details of a user held in Delius Identity (LDAP). Requires one of ROLE_COMMUNITY_AUTH_INT,ROLE_COMMUNITY_USERS,ROLE_COMMUNITY_USERS_ROLES")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires any of ROLE_COMMUNITY_AUTH_INT,ROLE_COMMUNITY_USERS,ROLE_COMMUNITY_USERS_ROLES"),
            @ApiResponse(responseCode = "404", description = "Not Found")
        })
    @PreAuthorize("hasAnyRole('ROLE_COMMUNITY_AUTH_INT','ROLE_COMMUNITY_USERS','ROLE_COMMUNITY_USERS_ROLES','ROLE_PROBATION_INTEGRATION_ADMIN')")
    @RequestMapping(value = "/users/{username}/details", method = RequestMethod.GET)
    public UserDetails findUser(@Parameter(name = "username", description = "LDAP username", example = "TESTUSERNPS", required = true) @NotNull final @PathVariable("username") String username) {
        return userService.getUserDetails(username)
            .orElseThrow(() -> new NotFoundException(String.format("User with username %s", username)));
    }
}
