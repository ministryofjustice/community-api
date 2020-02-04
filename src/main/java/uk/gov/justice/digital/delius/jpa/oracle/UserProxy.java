package uk.gov.justice.digital.delius.jpa.oracle;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
@Profile("oracle")
@Aspect
public class UserProxy {

    private final Advisor closeConnectionAdvisor;
    private final CurrentUserSupplier currentUserSupplier;

    @Autowired
    public UserProxy(@Qualifier("bclOracleCloseConnectionAdvice") Advisor closeConnectionAdvisor,
                     final CurrentUserSupplier currentUserSupplier) {
        this.closeConnectionAdvisor = closeConnectionAdvisor;
        this.currentUserSupplier = currentUserSupplier;
    }

    @Around("execution (* javax.sql.DataSource.getConnection(..))")
    public Connection doOracleProxyThing(ProceedingJoinPoint joinPoint) throws Throwable {
        Connection connection = (Connection) joinPoint.proceed(joinPoint.getArgs());

        return currentUserSupplier.username().map(username -> getConnection(connection, username)).orElse(connection);
    }

    private Connection getConnection(Connection connection, String uid) {
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
}