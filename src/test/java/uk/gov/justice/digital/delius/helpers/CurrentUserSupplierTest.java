package uk.gov.justice.digital.delius.helpers;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.SecurityUserContext;
import uk.gov.justice.digital.delius.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrentUserSupplierTest {
    @Mock
    private SecurityUserContext securityUserContext;

    private CurrentUserSupplier currentUserSupplier;

    @BeforeEach
    public void before() {
        currentUserSupplier = new CurrentUserSupplier(securityUserContext);
        CurrentUserSupplier.unsetClaims();
        CurrentUserSupplier.unsetNationalUserOverride();
    }

    @Test
    public void willReturnUserFromClaimWhenNotSecure() {
        when(securityUserContext.isSecure()).thenReturn(false);

        CurrentUserSupplier.setClaims(new DefaultClaims(Map.of(Jwt.UID, "testy.test")));

        assertThat(currentUserSupplier.username()).contains("testy.test");
    }

    @Test
    public void willReturnEmptyWhenNotSecureAndNoClaimPresent() {
        when(securityUserContext.isSecure()).thenReturn(false);

        CurrentUserSupplier.unsetClaims();

        assertThat(currentUserSupplier.username()).isNotPresent();
    }

    @Test
    public void willReturnUserFromSecurityContextWhenSecureAndWhenNonClientCall() {
        when(securityUserContext.isSecure()).thenReturn(true);
        when(securityUserContext.isClientOnly()).thenReturn(false);
        when(securityUserContext.getCurrentUsername()).thenReturn(Optional.of("testy.test"));


        assertThat(currentUserSupplier.username()).contains("testy.test");
    }

    @Test
    public void willReturnAPIUserWhenSecureAndWhenClientCall() {
        when(securityUserContext.isSecure()).thenReturn(true);
        when(securityUserContext.isClientOnly()).thenReturn(true);


        assertThat(currentUserSupplier.username()).contains("APIUser");
    }

    @Test
    public void willReturnDatabaseUserWhenSecureAndWhenClientCall() {
        when(securityUserContext.isSecure()).thenReturn(true);
        when(securityUserContext.isClientOnly()).thenReturn(true);
        when(securityUserContext.getDatabaseUsername()).thenReturn("MOIC");

        assertThat(currentUserSupplier.username()).contains("MOIC");
    }

    @Test
    public void willReturnNationalUserFromClaimWhenNotSecureAndNationalOverride() {
        // the implementation means the below will not be called but leave it
        // here for clarity on the scenario
        lenient().when(securityUserContext.isSecure()).thenReturn(false);
        CurrentUserSupplier.setClaims(new DefaultClaims(Map.of(Jwt.UID, "testy.test")));

        CurrentUserSupplier.setNationalUserOverride();

        assertThat(currentUserSupplier.username()).contains("NationalUser");
    }

    @Test
    public void willReturnNationalUserWhenNotSecureAndNoClaimPresentAndNationalOverride() {
        lenient().when(securityUserContext.isSecure()).thenReturn(false);

        CurrentUserSupplier.unsetClaims();
        CurrentUserSupplier.setNationalUserOverride();

        assertThat(currentUserSupplier.username()).get().isEqualTo("NationalUser");
    }

    @Test
    public void willReturnNationalUserFromSecurityContextWhenSecureAndWhenNonClientCallAndNationalOverride() {
        lenient().when(securityUserContext.isSecure()).thenReturn(true);
        lenient().when(securityUserContext.isClientOnly()).thenReturn(false);
        lenient().when(securityUserContext.getCurrentUsername()).thenReturn(Optional.of("testy.test"));

        CurrentUserSupplier.setNationalUserOverride();

        assertThat(currentUserSupplier.username()).contains("NationalUser");
    }

    @Test
    public void willReturnNationalUserWhenSecureAndWhenClientCallAndNationalOverride() {
        lenient().when(securityUserContext.isSecure()).thenReturn(true);
        lenient().when(securityUserContext.isClientOnly()).thenReturn(true);

        CurrentUserSupplier.setNationalUserOverride();

        assertThat(currentUserSupplier.username()).contains("NationalUser");
    }
}