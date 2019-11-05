package uk.gov.justice.digital.delius.helpers;

import com.google.common.collect.ImmutableMap;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.config.SecurityUserContext;
import uk.gov.justice.digital.delius.jwt.Jwt;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrentUserSupplierTest {
    @Mock
    private SecurityUserContext securityUserContext;

    private CurrentUserSupplier currentUserSupplier;

    @Before
    public void before() {
        currentUserSupplier = new CurrentUserSupplier(securityUserContext);
        CurrentUserSupplier.unsetClaims();
        CurrentUserSupplier.unsetNationalUserOverride();
    }

    @Test
    public void willReturnUserFromClaimWhenNotSecure() {
        when(securityUserContext.isSecure()).thenReturn(false);

        CurrentUserSupplier.setClaims(new DefaultClaims(ImmutableMap.of(Jwt.UID, "testy.test")));

        assertThat(currentUserSupplier.username()).get().isEqualTo("testy.test");
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


        assertThat(currentUserSupplier.username()).get().isEqualTo("testy.test");
    }

    @Test
    public void willReturnAPIUserWhenSecureAndWhenClientCall() {
        when(securityUserContext.isSecure()).thenReturn(true);
        when(securityUserContext.isClientOnly()).thenReturn(true);


        assertThat(currentUserSupplier.username()).get().isEqualTo("APIUser");
    }

    @Test
    public void willReturnNationalUserFromClaimWhenNotSecureAndNationalOverride() {
        // the implementation means the below will not be called but leave it
        // here for clarity on the scenario
        lenient().when(securityUserContext.isSecure()).thenReturn(false);
        CurrentUserSupplier.setClaims(new DefaultClaims(ImmutableMap.of(Jwt.UID, "testy.test")));

        CurrentUserSupplier.setNationalUserOverride();

        assertThat(currentUserSupplier.username()).get().isEqualTo("NationalUser");
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

        assertThat(currentUserSupplier.username()).get().isEqualTo("NationalUser");
    }

    @Test
    public void willReturnNationalUserWhenSecureAndWhenClientCallAndNationalOverride() {
        lenient().when(securityUserContext.isSecure()).thenReturn(true);
        lenient().when(securityUserContext.isClientOnly()).thenReturn(true);

        CurrentUserSupplier.setNationalUserOverride();

        assertThat(currentUserSupplier.username()).get().isEqualTo("NationalUser");
    }
}