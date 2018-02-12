package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class UserAndLdap {
    private User user;
    private List<Map<String, String>> ldapMatches;

}
