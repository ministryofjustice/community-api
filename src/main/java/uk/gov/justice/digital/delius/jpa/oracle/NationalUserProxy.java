package uk.gov.justice.digital.delius.jpa.oracle;

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
public class NationalUserProxy {

    @Before("execution(@uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride * *(..))")
    public void setNationalUserOverride(JoinPoint joinPoint) {
        CurrentUserSupplier.setNationalUserOverride();
    }

    @After("execution(@uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride * *(..))")
    public void unsetNationalUserOverride(JoinPoint joinPoint) {
        CurrentUserSupplier.unsetNationalUserOverride();
    }

}