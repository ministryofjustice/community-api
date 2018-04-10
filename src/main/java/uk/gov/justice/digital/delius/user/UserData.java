package uk.gov.justice.digital.delius.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserData {
    private String distinguishedName;
    private String uid;
    private List<String> probationAreaCodes;
}
