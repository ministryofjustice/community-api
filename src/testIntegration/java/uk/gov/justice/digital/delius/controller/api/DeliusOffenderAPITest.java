package uk.gov.justice.digital.delius.controller.api;


import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapperType;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusMockServer;
import uk.gov.justice.digital.delius.data.api.Address;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Optional;
import java.util.UUID;


import static io.restassured.RestAssured.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.justice.digital.delius.OffenderHelper.anOffender;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"offender.ids.pagesize=5"})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev-seed")
@Import(uk.gov.justice.digital.delius.FlywayKickConfig.class)
public class DeliusOffenderAPITest {

    private static DeliusMockServer deliusMockServer = new DeliusMockServer(8088, "src/testIntegration/resources");

    @RegisterExtension
    static DeliusExtension deliusExtension = new DeliusExtension(deliusMockServer);

    @LocalServerPort
    int port;

    @MockitoBean
    private OffenderRepository offenderRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_3)
            .jackson3ObjectMapperFactory(
                (aClass, s) -> jsonMapper
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
