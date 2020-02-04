package uk.gov.justice.digital.delius.jpa.oracle;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Profile("oracle")
@Configuration
public class OracleConfig {

    @Bean
    public AspectJExpressionPointcutAdvisor bclOracleCloseConnectionAdvice() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression("execution (* java.sql.Connection.close(..))");
        advisor.setAdvice((MethodBeforeAdvice) (method, args, target) -> {
            if (method.getName().equals("close")) {
                final PreparedStatement stmt = (Connection.class.cast(target)).prepareStatement("call PKG_VPD_CTX.CLEAR_CLIENT_IDENTIFIER()");
                stmt.execute();
                stmt.close();
            }
        });

        return advisor;
    }
}
