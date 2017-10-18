package uk.gov.justice.digital.delius.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserData {
    private String distinguishedName;
}
