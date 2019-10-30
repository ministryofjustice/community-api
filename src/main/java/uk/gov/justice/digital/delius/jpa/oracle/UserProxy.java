package uk.gov.justice.digital.delius.jpa.oracle;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.config.SecurityUserContext;
import uk.gov.justice.digital.delius.jwt.Jwt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static uk.gov.justice.digital.delius.utils.UserMdcFilter.USER_ID_HEADER;

@Component
@Profile("oracle")
@Aspect
@Slf4j
public class UserProxy {

    public static final ThreadLocal<Claims> threadLocalClaims = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> threadLocalNationalUserOverride = new ThreadLocal<>();
    public static final String NATIONAL_USER = "NationalUser";

    private final Advisor closeConnectionAdvisor;
    private final SecurityUserContext securityUserContext;

    @Autowired
    public UserProxy(@Qualifier("bclOracleCloseConnectionAdvice") Advisor closeConnectionAdvisor,
                     final SecurityUserContext securityUserContext) {
        this.closeConnectionAdvisor = closeConnectionAdvisor;
        this.securityUserContext = securityUserContext;
    }

    @Around("execution (* javax.sql.DataSource.getConnection(..))")
    public Connection doOracleProxyThing(ProceedingJoinPoint joinPoint) throws Throwable {
        Connection connection = (Connection) joinPoint.proceed(joinPoint.getArgs());

        return securityUserContext.getCurrentUsername()
                .map(u -> {
                    final String username = MDC.get(USER_ID_HEADER);
                    return getConnection(connection,  username != null ? username : NATIONAL_USER);
                }).orElseGet(
                        () -> {
                            Optional<Claims> maybeClaims = Optional.ofNullable(threadLocalClaims.get());
                            Boolean overrideNationalUser = Optional.ofNullable(threadLocalNationalUserOverride.get()).orElse(false);

                            if (overrideNationalUser) {
                                return getConnection(connection, NATIONAL_USER);
                            }

                            return maybeClaims.map(claims -> {
                                String uid = claims.get(Jwt.UID).toString();
                                return getConnection(connection, uid);
                            }).orElse(connection);
                        });
    }

    private Connection getConnection(Connection connection, String uid) {
        log.info("Doing oracle vpd thing using uid {}", uid);

        ProxyFactory proxyFactory = new ProxyFactory(connection);
        proxyFactory.addAdvisor(closeConnectionAdvisor);

        Connection proxiedConnection = (Connection) proxyFactory.getProxy();

        try (PreparedStatement stmt = proxiedConnection.prepareStatement("call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?)")) {
            stmt.setString(1, uid);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return proxiedConnection;
    }

    public static String username() {
        Optional<Claims> maybeClaims = Optional.ofNullable(threadLocalClaims.get());

        return maybeClaims.map(claims -> claims.get(Jwt.UID).toString()).orElseThrow(() -> new RuntimeException("No user found in context"));
    }
}