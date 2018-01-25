package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EXCLUSION")
public class Exclusion {

    @Id
    @Column(name = "EXCLUSION_ID")
    private Long exclusionId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "EXCLUSION_END_TIME")
    private LocalDateTime exclusionEnd;

    public boolean isActive() {
        return exclusionEnd == null || exclusionEnd.isAfter(LocalDateTime.now());
    }
}
