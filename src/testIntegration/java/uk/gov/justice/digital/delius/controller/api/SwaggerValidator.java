package uk.gov.justice.digital.delius.controller.api;

import io.swagger.v3.parser.converter.SwaggerConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SwaggerValidator {
    @LocalServerPort
    private int port;

    @Test
    public void test() {
        final var result = new SwaggerConverter().readLocation(String.format("http://localhost:%d/v2/api-docs", port), null, null);
        assertThat(result.getMessages()).isEmpty();
    }
}
