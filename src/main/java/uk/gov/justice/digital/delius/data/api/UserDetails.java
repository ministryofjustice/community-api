package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDetails {
    private String surname;
    private String firstName;
    private String email;
    private List<UserRole> roles;
}
