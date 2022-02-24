package uk.gov.justice.digital.delius.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
@Pattern(regexp = "^[A-Z0-9]{7}$")
public @interface StaffCode {
    String message() default "must be 7 alphanumeric characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
