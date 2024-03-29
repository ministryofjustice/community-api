package uk.gov.justice.digital.delius.jpa.national.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "RESTRICTION")
public class Restriction {

    @Id
    @Column(name = "RESTRICTION_ID")
    private Long restrictionId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "RESTRICTION_END_TIME")
    private LocalDateTime restrictionEnd;

    public boolean isActive() {
        return restrictionEnd == null || restrictionEnd.isAfter(LocalDateTime.now());
    }

}
