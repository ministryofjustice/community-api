package uk.gov.justice.digital.delius.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.justice.digital.delius.config.ApplicationConfig;
import uk.gov.justice.digital.delius.data.api.alfresco.DocumentMeta;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoServiceTest {

    private AlfrescoService alfrescoService;

    private WebClient webClient;

    @Mock
    private ExchangeFunction exchangeFunction;

    private ObjectMapper objectMapper = ApplicationConfig.customiseObjectMapper(new ObjectMapper());

    @Before
    public void configureServiceWithMockableWebclient() {
        webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        alfrescoService = new AlfrescoService(webClient, "any_remote_user", "any_real_user");
    }

    @Test
    public void shouldBeNotFoundIfDocumentDOesNotBelongToOffender() throws JsonProcessingException {
        final var documentMeta = objectMapper.writeValueAsString(
                DocumentMeta.builder()
                        .crn("T1234")
                        .name("document.pdf")
                        .build());
        givenMockResponses(documentMeta);

        assertThat(alfrescoService.getDocument("123", "X9999").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnResourceWhenFound() throws IOException {
        final var documentMeta = objectMapper.writeValueAsString(
                DocumentMeta.builder()
                        .crn("T1234")
                        .name("document.pdf")
                        .build());
        final var fetchResponse = "abc";
        givenMockResponses(documentMeta, fetchResponse);

        final var response = alfrescoService.getDocument("123", "T1234");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().contentLength()).isEqualTo(3);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=\"document.pdf\"");
    }

    private void givenMockResponses(final String... responseBodies) {
        final var mockResponses = Arrays.stream(responseBodies)
                .map(this::getClientResponse)
                .toArray(Mono[]::new);

        if (mockResponses.length == 1) {
            when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(mockResponses[0]);
        } else {
            when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(mockResponses[0], Arrays.copyOfRange(mockResponses, 1, mockResponses.length));
        }
    }

    private Mono<ClientResponse> getClientResponse(final String body) {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("content-type", "application/json")
                .body(body)
                .build());
    }

}
