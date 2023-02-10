package uk.gov.justice.digital.delius.jpa.standard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.YesNoConverter;
import uk.gov.justice.digital.delius.jpa.standard.YesNoBlank;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "R_CONTACT_TYPE")
public class ContactType {
    @Id
    @Column(name = "CONTACT_TYPE_ID")
    private long contactTypeId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SHORT_DESCRIPTION")
    private String shortDescription;

    @Column(name = "ATTENDANCE_CONTACT")
    @Convert(converter = YesNoConverter.class)
    private Boolean attendanceContact;

    @Column(name = "NATIONAL_STANDARDS_CONTACT")
    @Convert(converter = YesNoConverter.class)
    private Boolean nationalStandardsContact;

    @Column(name = "CONTACT_ALERT_FLAG")
    private String alertFlag;

    @Column(name = "SELECTABLE", nullable = false, length = 1)
    @Convert(converter = YesNoConverter.class)
    private Boolean selectable;

    /**
     * This is used in Delius to populate the list of available contact types on the schedule future appointments feature.
     * This should be used when checking if a contact type is appropriate for a logical appointment operation e.g. booking recurring, cancelling.
     */
    @Column(name = "FUTURE_SCHEDULED_CONTACTS_FLAG", nullable = false, length = 1)
    private String scheduleFutureAppointments;

    @Column(name = "CONTACT_LOCATION_FLAG", columnDefinition = "CHAR(1)", nullable = false)
    @Enumerated(EnumType.STRING)
    private YesNoBlank locationFlag;

    @Column(name = "CJA_ORDERS", nullable = false, length = 1)
    private String cjaOrderLevel;

    @Column(name = "LEGACY_ORDERS", nullable = false, length = 1)
    private String  legacyOrderLevel;

    @Column(name = "OFFENDER_LEVEL_CONTACT", nullable = false, length = 1)
    @Convert(converter = YesNoConverter.class)
    private Boolean wholeOrderLevel;

    @Column(name = "OFFENDER_EVENT_0", nullable = false, length = 1)
    @Convert(converter = YesNoConverter.class)
    private Boolean offenderLevel;

    @Column(name = "SGC_FLAG")
    private Boolean systemGenerated;

    @Column(name = "CONTACT_OUTCOME_FLAG", columnDefinition = "CHAR(1)", nullable = false)
    @Enumerated(EnumType.STRING)
    private YesNoBlank outcomeFlag;

    @ManyToMany()
    @JoinTable(
        name = "R_CONTACT_TYPECONTACT_CATEGORY",
        joinColumns = {@JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)},
        inverseJoinColumns = {@JoinColumn(name = "STANDARD_REFERENCE_LIST_ID", nullable = false)}
    )
    private List<StandardReference> contactCategories;

    @ManyToMany
    @JoinTable(
        name = "R_CON_TYPE_REQ_TYPE_MAINCAT",
        joinColumns = {@JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)},
        inverseJoinColumns = {@JoinColumn(name = "RQMNT_TYPE_MAIN_CATEGORY_ID", nullable = false)}
    )
    private List<RequirementTypeMainCategory> requirementTypeCategories;

    @ManyToMany()
    @JoinTable(
        name = "R_CONTACT_TYPE_OUTCOME",
        joinColumns = {@JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)},
        inverseJoinColumns = {@JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID", nullable = false)}
    )
    private List<ContactOutcomeType> contactOutcomeTypes;
}
