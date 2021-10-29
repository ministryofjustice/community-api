package uk.gov.justice.digital.delius.controller.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.time.LocalTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DeliusApiMockServer extends WireMockServer {

    public DeliusApiMockServer(final int port) {
        super(WireMockConfiguration.wireMockConfig().port(port));
    }

    public DeliusApiMockServer(final int port, final String fileDirectory) {
        super(WireMockConfiguration.wireMockConfig().port(port).usingFilesUnderDirectory(fileDirectory).jettyStopTimeout(10000L));
    }

    public void stubPostNsiToDeliusApi() {
        stubFor(post(urlPathMatching("/v1/nsi")).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(201)
            .withBody("{\n" +
                "    \"id\": 2500029015,\n" +
                "    \"type\": \"KSS021\",\n" +
                "    \"subType\": \"KSS026\",\n" +
                "    \"offenderCrn\": \"X371505\",\n" +
                "    \"eventId\": 2500428188,\n" +
                "    \"requirementId\": 2500185175,\n" +
                "    \"referralDate\": \"2021-03-01\",\n" +
                "    \"expectedStartDate\": \"2021-03-01\",\n" +
                "    \"expectedEndDate\": \"2021-03-01\",\n" +
                "    \"startDate\": \"2021-03-01\",\n" +
                "    \"endDate\": \"2021-03-01\",\n" +
                "    \"length\": 1,\n" +
                "    \"status\": \"SLI01\",\n" +
                "    \"statusDate\": \"2021-03-03T00:01:00\",\n" +
                "    \"outcome\": \"COMP\",\n" +
                "    \"notes\": \"fugi\",\n" +
                "    \"intendedProvider\": \"C21\",\n" +
                "    \"manager\": {\n" +
                "        \"id\": 2500039895,\n" +
                "        \"provider\": \"C00\",\n" +
                "        \"team\": \"C00T02\",\n" +
                "        \"staff\": \"C00P017\"\n" +
                "    }\n" +
                "}")
        ));
    }

    public void stubPostNewDocumentToDeliusApi() {
        stubFor(post(urlPathMatching("/v1/offenders/X320741/contacts/17666633/documents"))
            .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(201)
            .withBody("""
                {
                    "author": "Author name",
                    "authorProvider": "Author Provider",
                    "createdProvider": "Created Provider",
                    "creationDate": "2021-10-28T13:50:36.787Z",
                    "crn": "X320741",
                    "dateLastModified": "2021-10-28T13:50:36.787Z",
                    "docRenamed": true,
                    "documentName": "upwDocument.pdf",
                    "documentType": "DOCUMENT",
                    "entityId": 12882882,
                    "entityType": "CONTACT",
                    "id": 272732,
                    "lastModifiedUser": "last user",
                    "lockOwner": "Firstname Lastname",
                    "locked": true,
                    "remoteDocumentId": "bc1cea0f-93bd-44f2-8f35-16859da49e71",
                    "reservationOwner": "owner lastname",
                    "reserved": true
                }
                """)
        ));
    }


    public void stubPatchNsiToDeliusApi() {
        stubFor(patch(urlPathMatching("/v1/nsi/2500018596")).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody("{\n" +
                "    \"id\": 2500295345,\n" +
                "    \"type\": \"KSS021\",\n" +
                "    \"subType\": \"KSS026\",\n" +
                "    \"offenderCrn\": \"X371505\",\n" +
                "    \"eventId\": 2500428188,\n" +
                "    \"requirementId\": 2500185175,\n" +
                "    \"referralDate\": \"2021-03-01\",\n" +
                "    \"expectedStartDate\": \"2021-03-01\",\n" +
                "    \"expectedEndDate\": \"2021-03-01\",\n" +
                "    \"startDate\": \"2021-03-01\",\n" +
                "    \"endDate\": \"2021-03-01\",\n" +
                "    \"length\": 1,\n" +
                "    \"status\": \"SLI01\",\n" +
                "    \"statusDate\": \"2021-03-03T00:01:00\",\n" +
                "    \"outcome\": \"COMP\",\n" +
                "    \"notes\": \"fugi\",\n" +
                "    \"intendedProvider\": \"C21\",\n" +
                "    \"manager\": {\n" +
                "        \"id\": 2500039895,\n" +
                "        \"provider\": \"C00\",\n" +
                "        \"team\": \"C00T02\",\n" +
                "        \"staff\": \"C00P017\"\n" +
                "    }\n" +
                "}")
        ));
    }

    public void stubPostContactToDeliusApi() {
        stubFor(post(urlPathMatching("/v1/contact")).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(201)
            .withBody("""
                {
                    "id": 17666633,
                    "offenderCrn": "X320741",
                    "type": "CRSAPT",
                    "typeDescription": "Appointment with CRS Provider (NS)",
                    "provider": "CRS",
                    "team": "CRSUAT",
                    "staff": "CRSUATU",
                    "officeLocation": "CRSSHEF",
                    "date": "2021-03-01",
                    "startTime": "13:01:02",
                    "endTime": "14:03:04",
                    "notes": "http://url",
                    "eventId": 2500295343,
                    "requirementId": 2500428188,
                    "sensitive": true
                }""")
        ));
    }

    public void stubReplaceContactToDeliusApi() {
        stubFor(post(urlPathMatching("/v1/contact/2512709905/replace")).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(201)
            .withBody("{\n" +
                "    \"id\": 2500029016,\n" +
                "    \"offenderCrn\": \"X320741\",\n" +
                "    \"type\": \"CRSAPT\",\n" +
                "    \"typeDescription\": \"Appointment with CRS Provider (NS)\",\n" +
                "    \"provider\": \"CRS\",\n" +
                "    \"team\": \"CRSUAT\",\n" +
                "    \"staff\": \"CRSUATU\",\n" +
                "    \"officeLocation\": \"CRSSHEF\",\n" +
                "    \"date\": \"2021-03-01\",\n" +
                "    \"startTime\": \"13:01:02\",\n" +
                "    \"endTime\": \"14:03:04\",\n" +
                "    \"notes\": \"http://url\",\n" +
                "    \"eventId\": 2500295343,\n" +
                "    \"requirementId\": 2500428188\n" +
                "    }\n" +
                "}")
        ));
    }

    public void stubPatchContactToDeliusApi(Long appointmentId) {
        stubFor(patch(urlPathMatching("/v1/contact/" + appointmentId)).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody("{\n" +
                "    \"id\": " + appointmentId + ",\n" +
                "    \"offenderCrn\": \"X320741\",\n" +
                "    \"type\": \"CRSAPT\",\n" +
                "    \"provider\": \"CRS\",\n" +
                "    \"team\": \"CRSUAT\",\n" +
                "    \"staff\": \"CRSUATU\",\n" +
                "    \"officeLocation\": \"CRSSHEF\",\n" +
                "    \"date\": \"2021-03-01\",\n" +
                "    \"startTime\": \"13:01:02\",\n" +
                "    \"endTime\": \"14:03:04\",\n" +
                "    \"notes\": \"http://url\",\n" +
                "    \"eventId\": 2500295343,\n" +
                "    \"requirementId\": 2500428188\n" +
                "    }\n" +
                "}")
        ));
    }

    public void stubDeleteContactToDeliusApi() {
        stubFor(delete(urlPathMatching("/v1/contact/2502709999")).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
        ));
    }
}
