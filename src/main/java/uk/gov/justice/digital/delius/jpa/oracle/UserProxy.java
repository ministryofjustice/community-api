package uk.gov.justice.digital.delius.jpa.oracle;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public final Advisor closeConnectionAdvisor;

    @Autowired
    public UserProxy(@Qualifier("bclOracleCloseConnectionAdvice") Advisor closeConectionAdvisor) {
        this.closeConnectionAdvisor = closeConectionAdvisor;
    }

    @Around("execution (* javax.sql.DataSource.getConnection(..))")
    public Connection doOracleProxyThing(ProceedingJoinPoint joinPoint) throws Throwable {
        Connection connection = (Connection) joinPoint.proceed(joinPoint.getArgs());
        Optional<Claims> maybeClaims = Optional.ofNullable(threadLocalClaims.get());

        return maybeClaims.map(claims -> {

            log.info("Doing oracle vpd thing for user " + claims.getSubject());
            ProxyFactory proxyFactory = new ProxyFactory(connection);
            proxyFactory.addAdvisor(closeConnectionAdvisor);

            Connection proxiedConnection = (Connection) proxyFactory.getProxy();

            try (PreparedStatement stmt = proxiedConnection.prepareStatement("call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER('" + claims.get("deliusDistinguishedName") + "')");){
                stmt.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return proxiedConnection;
        }).orElse(connection);
    }
}