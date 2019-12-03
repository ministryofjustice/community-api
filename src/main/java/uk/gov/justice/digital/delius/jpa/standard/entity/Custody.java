package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CUSTODY")
public class Custody {
    @Id
    @Column(name = "CUSTODY_ID")
    private Long custodyId;

    @JoinColumn(name = "DISPOSAL_ID", referencedColumnName = "DISPOSAL_ID")
    @OneToOne
    private Disposal disposal;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "PRISONER_NUMBER")
    private String prisonerNumber;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "INSTITUTION_ID"),
            @JoinColumn(name = "ESTABLISHMENT")})
    private RInstitution institution;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval=true, mappedBy = "custody")
    private List<KeyDate> keyDates;

    @OneToMany
    @JoinColumn(name = "CUSTODY_ID")
    private List<Release> releases;

    public Optional<Release> findLatestRelease() {
        return this.getReleases().stream()
                .filter(not(Release::isSoftDeleted))
                .max(Comparator.comparing(Release::getActualReleaseDate));
    }
}
