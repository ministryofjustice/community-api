package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.AppointmentDetail;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Explanation;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceCondition;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.util.EntityHelper;
import uk.gov.justice.digital.delius.utils.DateConverter;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AppointmentTransformerTest {
    @Test
    public void appointmentOutcomeMappedFromContactOutcomeType() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                    .toBuilder()
                    .contactOutcomeType(ContactOutcomeType
                            .builder()
                            .code("XX")
                            .description("Did not turn up")
                            .build())
                .build())).get(0).getAppointmentOutcomeType())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "XX")
                .hasFieldOrPropertyWithValue("description", "Did not turn up");
    }

    @Test
    public void appointmentTypeMappedFromContactType() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                    .toBuilder()
                    .contactType(ContactType
                            .builder()
                            .code("XX")
                            .description("Accommodation")
                            .build())
                .build())).get(0).getAppointmentType())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "XX")
                .hasFieldOrPropertyWithValue("description", "Accommodation");
    }
    @Test
    public void officeLocationMappedFromOfficeLocation() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                    .toBuilder()
                    .officeLocation(OfficeLocation
                            .builder()
                            .code("XX")
                            .description("Barnet")
                            .build())
                .build())).get(0).getOfficeLocation())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "XX")
                .hasFieldOrPropertyWithValue("description", "Barnet");
    }

    @Test
    public void attendedMappedToNotRecordedWhenNull() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended(null)
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.NOT_RECORDED);

    }

    @Test
    public void attendedMappedToAttendedWhenY() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended("Y")
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.ATTENDED);

    }

    @Test
    public void attendedMappedToNotAttendedWhenN() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended("N")
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.UNATTENDED);

    }

    @Test
    public void nationalStandardMappedToNsiType() {

        assertThat(AppointmentTransformer.appointmentDetailOf(EntityHelper.aContact()
            .toBuilder()
            .contactType(ContactType.builder().code("123").description("National Standard")
                .nationalStandardsContact(true).cjaOrderLevel("Y")
                .legacyOrderLevel("Y").build())
            .build())).hasFieldOrPropertyWithValue("type.nsi", true);
    }

    @Test
    public void rarRequirementMappedFromRarRequirement() {
        final var requirement = EntityHelper.aRarRequirement();
        final var source = EntityHelper.aContact()
            .toBuilder()
            .nsi(EntityHelper.aNsi().toBuilder().rqmnt(requirement).build())
            .build();

        final var observed = AppointmentTransformer.appointmentDetailOf(source);

        assertThat(observed)
            .hasFieldOrPropertyWithValue("requirement.requirementId", requirement.getRequirementId())
            .hasFieldOrPropertyWithValue("requirement.isRar", true)
            .hasFieldOrPropertyWithValue("requirement.isActive", true);
    }

    @Test
    public void appointmentDetailFromContactHandlesNulls() {
        final var contact = EntityHelper.aContact().toBuilder()
            .contactStartTime(null)
            .contactEndTime(null)
            .officeLocation(null)
            .notes(null)
            .sensitive(null)
            .contactOutcomeType(null)
            .rarActivity(null)
            .nsi(null)
            .build();
        final var observed = AppointmentTransformer.appointmentDetailOf(contact);
        final var expectedDate = DateConverter.toOffsetDateTime(LocalDateTime.of(contact.getContactDate(), LocalTime.MIDNIGHT));
        assertThat(observed)
            .isNotNull()
            .isInstanceOf(AppointmentDetail.class)
            .hasFieldOrPropertyWithValue("appointmentStart", expectedDate)
            .hasFieldOrPropertyWithValue("appointmentStart", expectedDate)
            .hasFieldOrPropertyWithValue("officeLocation", null)
            .hasFieldOrPropertyWithValue("notes", null)
            .hasFieldOrPropertyWithValue("sensitive", null)
            .hasFieldOrPropertyWithValue("outcome", null)
            .hasFieldOrPropertyWithValue("rarActivity", null)
            .hasFieldOrPropertyWithValue("requirement", null);
    }

    private Contact aContact() {
        return Contact
                .builder()
                .contactOutcomeType(ContactOutcomeType
                        .builder()
                        .build())
                .contactType(ContactType
                        .builder()
                        .build())
                .explanation(Explanation
                        .builder()
                        .build())
                .licenceCondition(LicenceCondition
                        .builder()
                        .build())
                .probationArea(ProbationArea
                        .builder()
                        .build())
                .team(Team
                        .builder()
                        .build())
                .requirement(Requirement
                        .builder()
                        .build())
                .staff(Staff
                        .builder()
                        .build())
                .build();
    }
}
