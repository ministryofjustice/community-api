package uk.gov.justice.digital.delius;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class DeliusOffenderAPI {

    public static void main(String[] args) {
        SpringApplication.run(DeliusOffenderAPI.class, args);
    }

}