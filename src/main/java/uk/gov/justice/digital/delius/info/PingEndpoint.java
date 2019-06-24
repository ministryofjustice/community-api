package uk.gov.justice.digital.delius.info;


import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.stereotype.Component;

@Component
public class PingEndpoint implements Endpoint<String> {

    @Override
    public String getId() {
        return "ping";
    }

    @Override
    public String invoke() {
        return "pong";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }
}