package uk.gov.justice.digital.delius.jwt;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(code = 200, message = "Happy path"),
        @ApiResponse(code = 401, message = "User JWT is missing"),
        @ApiResponse(code = 403, message = "User JWT has failed validation"),
        @ApiResponse(code = 404, message = "Entity not found")

})
public @interface JwtValidation {
}
