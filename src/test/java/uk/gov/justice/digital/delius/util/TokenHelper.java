package uk.gov.justice.digital.delius.util;

import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.UUID;

public interface TokenHelper {
    default String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    default String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt().buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }

    Jwt jwt();

}
