package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder(toBuilder = true)
@Table(name = "KEY_DATE")
public class KeyDate {
    private static final String LICENCE_EXPIRY_DATE_CODE = "LED";
    private static final String SENTENCE_EXPIRY_DATE_CODE = "SED";
    @Id
    @SequenceGenerator(name = "KEY_DATE_ID_GENERATOR", sequenceName = "KEY_DATE_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "KEY_DATE_ID_GENERATOR")
    @Column(name = "KEY_DATE_ID")
    private Long keyDateId;
    @ManyToOne
    @JoinColumn(name = "CUSTODY_ID", nullable = false)
    @ToString.Exclude
    private Custody custody;
    @ManyToOne
    @JoinColumn(name = "KEY_DATE_TYPE_ID")
    private StandardReference keyDateType;
    @Column(name = "KEY_DATE")
    private LocalDate keyDate;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    public static boolean isSentenceExpiryKeyDate(String keyDateCode) {
        return SENTENCE_EXPIRY_DATE_CODE.equals(keyDateCode);
    }

    public boolean isLicenceExpiryDate() {
        return Optional.ofNullable(keyDateType).map(StandardReference::getCodeValue).filter(LICENCE_EXPIRY_DATE_CODE::equals).isPresent();
    }

    public boolean isDateInPast() {
        return Optional.ofNullable(keyDate).filter(date -> date.isBefore(LocalDate.now())).isPresent();
    }
}
