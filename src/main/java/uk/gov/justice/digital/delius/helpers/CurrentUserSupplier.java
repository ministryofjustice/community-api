package uk.gov.justice.digital.delius.helpers;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.config.SecurityUserContext;
import uk.gov.justice.digital.delius.jwt.Jwt;

import java.util.Optional;

@Component
public class CurrentUserSupplier {
    private static final ThreadLocal<Claims> threadLocalClaims = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> threadLocalNationalUserOverride = new ThreadLocal<>();

    public static final String NATIONAL_USER = "NationalUser";
    public static final String API_USER = "APIUser";


    private final SecurityUserContext securityUserContext;

    public CurrentUserSupplier(SecurityUserContext securityUserContext) {
        this.securityUserContext = securityUserContext;
    }

    public Optional<String> username() {
        Boolean overrideNationalUser = Optional.ofNullable(threadLocalNationalUserOverride.get()).orElse(false);

        if (overrideNationalUser) {
            return Optional.of(NATIONAL_USER);
        }

        if (securityUserContext.isSecure()) {
            if (securityUserContext.isClientOnly()) {
                // check JWT to see if database_username is set, else use API_USER
                final var databaseUsername = securityUserContext.getDatabaseUsername();
                return Optional.of(databaseUsername != null ? databaseUsername : API_USER);
            } else {
                return securityUserContext.getCurrentUsername();
            }
        } else {
            return Optional.ofNullable(threadLocalClaims.get())
                    .map(claims -> claims.get(Jwt.UID).toString());
        }
    }

    public static void setClaims(Claims claims) {
        threadLocalClaims.set(claims);
    }

    static void unsetClaims() {
        threadLocalClaims.remove();
    }

    public static void setNationalUserOverride() {
        CurrentUserSupplier.threadLocalNationalUserOverride.set(true);
    }

    public static void unsetNationalUserOverride() {
        CurrentUserSupplier.threadLocalNationalUserOverride.set(false);
    }
}
