package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "RELEASE")
public class Release {
    @Id
    @Column(name = "RELEASE_ID")
    private Long releaseId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "ACTUAL_RELEASE_DATE")
    private LocalDateTime actualReleaseDate;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "INSTITUTION_ID"),
            @JoinColumn(name = "ESTABLISHMENT")})
    private RInstitution institution;

    @Column(name = "NOTES")
    private String notes;

    @OneToMany
    @JoinColumn(name = "RELEASE_ID")
    private List<Recall> recalls;

    @OneToOne
    @JoinColumn(name = "RELEASE_TYPE_ID")
    private StandardReference releaseType;

    public Optional<Recall> findLatestRecall() {
        return this.getRecalls() == null ? Optional.empty() : this.getRecalls().stream().max(Comparator.comparing(Recall::getRecallDate));
    }

    public boolean isSoftDeleted() {
        return this.softDeleted != 0L;
    }
}
