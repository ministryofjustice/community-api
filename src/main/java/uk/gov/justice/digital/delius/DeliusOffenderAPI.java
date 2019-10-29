package uk.gov.justice.digital.delius;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
@Slf4j
public class DeliusOffenderAPI {

    public static void main(String[] args) {
        System.setProperty("hikaricp.configurationFile", "/hikari.properties");
        SpringApplication.run(DeliusOffenderAPI.class, args);
    }

}