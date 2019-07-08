package uk.gov.justice.digital.delius.ldap.repository.entity;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NDeliusRole {
    private String cn;
    private String description;
}
