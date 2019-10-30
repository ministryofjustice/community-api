package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Api(tags = "Legacy Health Checks", description = "Please use /health, /info, /pong in base path")
@RequestMapping(value = "/api", method = RequestMethod.GET)
public class LegacyHealthChecks {

    @RequestMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public String health() {
        return "{ \"status\": \"UP\" }";
    }

    @RequestMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public String info() {
        return "{ }";
    }

    @RequestMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return "pong";
    }

}
