package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusMockServer;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.Address;
import uk.gov.justice.digital.delius.data.api.Count;
import uk.gov.justice.digital.delius.data.api.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.jpa.national.entity.Exclusion;
import uk.gov.justice.digital.delius.jpa.national.entity.Restriction;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.justice.digital.delius.OffenderHelper.anOffender;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"offender.ids.pagesize=5"})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev-seed")
public class DeliusOffenderAPITest {

    private static DeliusMockServer deliusMockServer = new DeliusMockServer(8088, "src/testIntegration/resources");

    @RegisterExtension
    static DeliusExtension deliusExtension = new DeliusExtension(deliusMockServer);

    @LocalServerPort
    int port;

    @MockBean
    private OffenderRepository offenderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
        Mockito.when(offenderRepository.findByOffenderId(any(Long.class))).thenReturn(Optional.empty());
        Mockito.when(offenderRepository.count()).thenReturn(666L);
    }

    @Test
    public void lookupKnownOffenderCRNDetailGivesFullFatOffender() {

        OffenderAddress mainAddress = OffenderAddress.builder()
                .streetName("Foo Street")
                .addressStatus(StandardReference.builder()
                        .codeValue("M")
                        .codeDescription("Main address").build())
                .build();
        Offender offender = anOffender().toBuilder()
                .offenderAddresses(asList(mainAddress)).build();
        Mockito.when(offenderRepository.findByCrn(eq("CRN123"))).thenReturn(Optional.of(offender));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/crn/CRN123/all")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
        assertThat(offenderDetail.getContactDetails().getAddresses()).isNotEmpty();

        Address address = offenderDetail.getContactDetails().getAddresses().get(0);
        assertThat(address.getStreetName()).isEqualTo("Foo Street");
        assertThat(address.getStatus().getCode()).isEqualTo("M");
        assertThat(address.getStatus().getDescription()).isEqualTo("Main address");
    }

    @Test
    public void lookupKnownOffenderCRNDetailGivesFullFatOffenderWithANullAddressStatus() {

        OffenderAddress mainAddress = OffenderAddress.builder()
                .streetName("Foo Street")
                .build();
        Offender offender = anOffender().toBuilder()
                .offenderAddresses(asList(mainAddress)).build();
        Mockito.when(offenderRepository.findByCrn(eq("CRN123"))).thenReturn(Optional.of(offender));

        OffenderDetail offenderDetail =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/offenders/crn/CRN123/all")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(OffenderDetail.class);

        assertThat(offenderDetail.getSurname()).isEqualTo("Sykes");
        assertThat(offenderDetail.getContactDetails().getAddresses()).isNotEmpty();

        Address address = offenderDetail.getContactDetails().getAddresses().get(0);
        assertThat(address.getStreetName()).isEqualTo("Foo Street");
        assertThat(address.getStatus()).isNull();
    }
    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }
}
