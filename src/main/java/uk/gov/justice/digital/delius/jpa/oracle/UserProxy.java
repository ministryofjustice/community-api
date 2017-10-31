package uk.gov.justice.digital.delius.jpa.oracle;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

@Component
@Profile("oracle")
@Aspect
@Slf4j
public class UserProxy {

    public static ThreadLocal<Claims> threadLocalClaims = new ThreadLocal<>();

    @Before("execution (* java.sql.Connection.close(..))")
    public void closeOracleProxyThing(JoinPoint joinPoint) throws SQLException {
        final Connection thisConnection = (Connection) joinPoint.getThis();
        log.info("Closing oracle vpd thing.");

        final PreparedStatement stmt = thisConnection.prepareStatement("call PKG_VPD_CTX.CLEAR_CLIENT_IDENTIFIER()");
        stmt.execute();
        stmt.close();
    }

    @Around("execution (* javax.sql.DataSource.getConnection(..))")
    public Connection doOracleProxyThing(ProceedingJoinPoint joinPoint) throws Throwable {
        final Connection connection = (Connection) joinPoint.proceed();

        Optional<Claims> maybeClaims = Optional.ofNullable(threadLocalClaims.get());

        return maybeClaims.map(claims -> {

            log.info("Doing oracle vpd thing for user " + claims.getSubject());

            final PreparedStatement stmt;
            try {
                stmt = connection.prepareStatement("call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER('" + claims.get("deliusDistinguishedName") + "')");
                stmt.execute();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return connection;
        }).orElse(connection);
    }
}