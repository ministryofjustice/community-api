package uk.gov.justice.digital.delius.service;

import com.github.fge.jsonpatch.JsonPatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewNsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewRecall;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewRelease;
import uk.gov.justice.digital.delius.data.api.deliusapi.NsiDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.ReplaceContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.UploadedDocumentDto;

import static org.springframework.web.util.UriComponentsBuilder.fromPath;

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

    public UploadedDocumentDto uploadDocument(final String crn, final Long contactId, final MultipartFile document) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fileData", document.getResource());
        return webClient.post()
            .uri("/v1/offenders/" + crn + "/contacts/" + contactId + "/documents")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .retrieve()
            .bodyToMono(UploadedDocumentDto.class)
            .block();
    }

    public NsiDto patchNsi(Long nsiId, JsonPatch jsonPatch) {
        return webClient.patch()
            .uri(fromPath("/v1/nsi/{id}").buildAndExpand(nsiId).toUriString())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(jsonPatch)
            .retrieve()
            .bodyToMono(NsiDto.class)
            .block();
    }

    public ContactDto createNewContact(NewContact newContact) {
        return webClient.post()
            .uri("/v1/contact")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(newContact)
            .retrieve()
            .bodyToMono(ContactDto.class)
            .block();
    }

    public ContactDto replaceContact(Long contactId, ReplaceContact replaceContact) {
        return webClient.post()
            .uri(fromPath("/v1/contact/{id}/replace").buildAndExpand(contactId).toUriString())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(replaceContact)
            .retrieve()
            .bodyToMono(ContactDto.class)
            .block();
    }

    public ContactDto patchContact(Long contactId, JsonPatch jsonPatch) {
        return webClient.patch()
            .uri(fromPath("/v1/contact/{id}").buildAndExpand(contactId).toUriString())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(jsonPatch)
            .retrieve()
            .bodyToMono(ContactDto.class)
            .block();
    }

    public Void deleteContact(Long contactId) {
        return webClient.delete()
            .uri(fromPath("/v1/contact/{id}").buildAndExpand(contactId).toUriString())
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
