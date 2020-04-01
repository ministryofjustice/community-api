package uk.gov.justice.digital.delius.ldap.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import javax.naming.Name;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Entry(objectClasses = "NDUser")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public final class NDeliusUser {
    @Id
    private Name dn;
    private String cn;
    private String sn;
    private String mail;
    private String givenname;
    private String endDate;
    private String userHomeArea;
    @Transient
    private List<NDeliusRole> roles;

    public boolean isEnabled() {
        return endDate == null || LocalDate.parse(endDate.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd")).isAfter(LocalDate.now());
    }
}
