package uk.gov.justice.digital.delius.utils;

import org.springframework.stereotype.Component;


@Component
public class UserContext {

    private static final ThreadLocal<String> authToken = new ThreadLocal<>();

    static String getAuthToken() {
        return authToken.get();
    }

    static void setAuthToken(final String aToken) {
        authToken.set(aToken);
    }
}
