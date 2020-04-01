package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.UserAreas;
import uk.gov.justice.digital.delius.service.UserService;

import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private UserService userService = mock(UserService.class);

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new UserController(userService),
                new SecureControllerAdvice()
        );
    }

    @Test
    public void willReturnTheAreas() {
        when(userService.getUserAreas("bobby.beans")).thenReturn(Optional.of(UserAreas
                .builder()
                .homeProbationArea("N02")
                .probationAreas(List.of("N01", "N02"))
                .build()));

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/users/bobby.beans/areas")
                .then()
                .statusCode(200)
                .body("homeProbationArea", equalTo("N02"))
                .body("probationAreas.size()", equalTo(2))
                .body("probationAreas[0]", equalTo("N01"))
                .body("probationAreas[1]", equalTo("N02"));
    }

    @Test
    public void willReturnNotFoundWhenUserMissing() {
        when(userService.getUserAreas("bobby.beans")).thenReturn(Optional.empty());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/users/bobby.beans/areas")
                .then()
                .statusCode(404);
    }

}
