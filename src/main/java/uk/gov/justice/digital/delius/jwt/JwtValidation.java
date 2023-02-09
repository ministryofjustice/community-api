package uk.gov.justice.digital.delius.jwt;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Happy path"),
        @ApiResponse(responseCode = "401", description = "User JWT is missing"),
        @ApiResponse(responseCode = "403", description = "User JWT has failed validation"),
        @ApiResponse(responseCode = "404", description = "Entity not found")

})
public @interface JwtValidation {
}
