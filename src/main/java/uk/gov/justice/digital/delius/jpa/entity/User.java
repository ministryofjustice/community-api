package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USER_")
public class User {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "FORENAME")
    private String forename;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "DISTINGUISHED_NAME")
    private String distinguishedName;

    @JoinColumn(name = "USER_ID")
    @OneToMany
    private List<Exclusion> exclusions;

    @JoinColumn(name = "USER_ID")
    @OneToMany
    private List<Restriction> restrictions;

    public boolean isExcludedFrom(Long offenderId) {
        return exclusions.stream().anyMatch(exclusion -> exclusion.getOffenderId().equals(offenderId) && exclusion.isActive());
    }

    public boolean isRestrictedUserFor(Long offenderId) {
        return restrictions.stream().anyMatch(restriction -> restriction.getOffenderId().equals(offenderId) && restriction.isActive());
    }

}
