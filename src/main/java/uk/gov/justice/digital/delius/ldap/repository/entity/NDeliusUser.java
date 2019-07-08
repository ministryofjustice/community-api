package uk.gov.justice.digital.delius.ldap.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;

@Data
@Entry(objectClasses = "NDUser")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NDeliusUser {
    @Id
    private Name dn;
    private String cn;
    private String sn;
    private String mail;
    private String givenname;
    @Transient
    private List<NDeliusRole> roles;
}
