package uk.gov.justice.digital.delius.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import org.apache.http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.controller.secure.IntegrationTestBase;

@Provider("community-api")
@PactBroker(url = "https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk")
class CourtCaseServiceVerificationPactTest extends IntegrationTestBase {

    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactTestTemplate(PactVerificationContext context, HttpRequest request) {
        request.addHeader("Authorization", "Bearer " + tokenWithRoleCommunity());
        context.verifyInteraction();
    }

    @State({"an NSI exists for CRN X320741 and conviction id 2500295345", "probation status detail is available for CRN X320741", "probation status detail is available for CRN CRN40"})
    void getState() {
    }

}
