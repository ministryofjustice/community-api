package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NsiDto;

@Service
@Slf4j
public class DeliusApiClient {
    private final WebClient webClient;

    @Autowired
    public DeliusApiClient(@Qualifier("deliusApiWebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    public NsiDto createNewNsi(final NewNsi newNsiRequest) {
        return webClient.post()
            .uri("/v1/nsi")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(newNsiRequest)
            .retrieve()
            .bodyToMono(NsiDto.class)
            .block();
    }
}
