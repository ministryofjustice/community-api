package uk.gov.justice.digital.delius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.justice.hmpps.kotlin.auth.HmppsResourceServerConfiguration;

@SpringBootApplication(exclude = HmppsResourceServerConfiguration.class)
public class DeliusOffenderAPI {

    static void main(String[] args) {
        System.setProperty("hikaricp.configurationFile", "/hikari.properties");
        SpringApplication.run(DeliusOffenderAPI.class, args);
    }

}