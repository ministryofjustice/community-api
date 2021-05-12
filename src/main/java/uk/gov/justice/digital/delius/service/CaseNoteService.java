package uk.gov.justice.digital.delius.service;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Service
@Log4j2
public class CaseNoteService {
    private final WebClient webClient;
    private static final Pattern CASE_NOTE_TYPE_PATTERN = Pattern.compile("noteType\"[ ]?:[ ]?\"([^\"]*)\"");

    @Autowired
    public CaseNoteService(@Qualifier("deliusWebClientWithAuth") final WebClient webClient) {
        this.webClient = webClient;
    }

    public ResponseEntity<String> upsertCaseNotesToDelius(final String nomisId, final Long caseNotesId, final String caseNote) {
        return webClient.put()
            .uri(uriBuilder -> uriBuilder.path("/nomisCaseNotes/{nomisId}/{caseNotesId}").build(nomisId, caseNotesId))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(caseNote)
            .retrieve()
            .toEntity(String.class)
            .onErrorResume(WebClientResponseException.class, e -> conflictWhenIgnoringOmicOpdError(caseNote, e))
            .block();
    }

    @NotNull
    private Mono<ResponseEntity<String>> conflictWhenIgnoringOmicOpdError(final String caseNote, final WebClientResponseException e) {
        if (e.getRawStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            final var matcher = CASE_NOTE_TYPE_PATTERN.matcher(caseNote);
            if (matcher.find()) {
                final var caseNoteType = matcher.group(1);
                if (caseNoteType.startsWith("OMIC_OPD")) {
                    log.warn("Ignoring Delius server error because we know Delius cannot handle NSI case notes of type {}", caseNoteType);
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                }
            }
        }
        return Mono.error(e);
    }
}
