package uk.gov.justice.digital.delius.jpa.oracle;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;

@Aspect
@Component
@Profile("oracle")
@Slf4j
public class NationalUserProxy {

    @Before("execution(@uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride * *(..))")
    public void setNationalUserOverride(JoinPoint joinPoint) {
        log.info("Overriding connection with NationalUser");
        CurrentUserSupplier.setNationalUserOverride();
    }

    @After("execution(@uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride * *(..))")
    public void unsetNationalUserOverride(JoinPoint joinPoint) {
        log.info("Clearing NationalUser override");
        CurrentUserSupplier.unsetNationalUserOverride();
    }

}