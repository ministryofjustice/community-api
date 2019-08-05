package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRole {
    private String name;
    private String description;
}
