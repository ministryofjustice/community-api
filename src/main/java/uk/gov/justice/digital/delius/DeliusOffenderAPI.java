package uk.gov.justice.digital.delius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeliusOffenderAPI {

    public static void main(String[] args) {
        System.setProperty("hikaricp.configurationFile", "/hikari.properties");
        SpringApplication.run(DeliusOffenderAPI.class, args);
    }

}