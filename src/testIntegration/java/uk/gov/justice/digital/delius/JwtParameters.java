package uk.gov.justice.digital.delius;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class JwtParameters {
    private String username;
    private String userId;
    @Builder.Default
    private List<String> scope = List.of();
    @Builder.Default
    private List<String> roles = List.of();
    @Builder.Default
    private Duration expiryTime = Duration.ofHours(1L);
    @Builder.Default
    private String jwtId = UUID.randomUUID().toString();
    @Builder.Default
    private String clientId = "community-api-client";
}
